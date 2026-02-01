package app.config;

import app.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

/**
 * WebSocket configuration for real-time notifications using STOMP protocol.
 * 
 * Architecture:
 * - Client connects via /ws endpoint
 * - Uses STOMP protocol for structured messaging
 * - JWT authentication on CONNECT
 * - User-specific destinations via /user/queue/...
 * - Broadcast destinations via /topic/...
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for subscriptions
        // /topic - for broadcast messages to all subscribers
        // /queue - for point-to-point messages to specific users
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages from clients to server (e.g., marking as read)
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        // Client subscribes to /user/queue/notifications
        // Server sends to /user/{userId}/queue/notifications
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint - clients connect here
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://localhost",
                        "http://localhost:80",
                        "https://*.azurecontainerapps.io"
                )
                .withSockJS(); // Fallback for browsers without WebSocket support
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Configure transport limits for better scalability
        registration.setMessageSizeLimit(64 * 1024); // 64KB max message size
        registration.setSendBufferSizeLimit(512 * 1024); // 512KB send buffer
        registration.setSendTimeLimit(20 * 1000); // 20 seconds send timeout
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Use Virtual Threads for processing incoming WebSocket messages
        SimpleAsyncTaskExecutor inboundExecutor = new SimpleAsyncTaskExecutor("ws-inbound-");
        inboundExecutor.setVirtualThreads(true);
        registration.executor(inboundExecutor);
        
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from Authorization header
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    
                    if (authHeaders == null || authHeaders.isEmpty()) {
                        log.warn("WebSocket CONNECT rejected: No Authorization header");
                        throw new AccessDeniedException("Missing authentication token");
                    }
                    
                    String authHeader = authHeaders.getFirst();
                    
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        log.warn("WebSocket CONNECT rejected: Invalid Authorization header format");
                        throw new AccessDeniedException("Invalid authentication token format");
                    }
                    
                    String jwt = authHeader.substring(7);
                    
                    try {
                        String userEmail = jwtService.extractUsername(jwt);
                        
                        if (userEmail == null) {
                            throw new AccessDeniedException("Invalid token");
                        }
                        
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                        
                        if (!jwtService.isTokenValid(jwt, userDetails)) {
                            throw new AccessDeniedException("Token expired or invalid");
                        }
                        
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        accessor.setUser(authToken);
                        
                        log.info("WebSocket CONNECT authenticated for user: {}", userEmail);
                        
                    } catch (AccessDeniedException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("WebSocket authentication failed", e);
                        throw new AccessDeniedException("Authentication failed: " + e.getMessage());
                    }
                }
                
                return message;
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Use Virtual Threads for sending outbound WebSocket messages
        // This ensures sending notifications to thousands of users doesn't block
        SimpleAsyncTaskExecutor outboundExecutor = new SimpleAsyncTaskExecutor("ws-outbound-");
        outboundExecutor.setVirtualThreads(true);
        registration.executor(outboundExecutor);
    }
}
