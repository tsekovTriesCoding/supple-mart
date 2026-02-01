package app.config;

import app.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for WebSocket session lifecycle events.
 * Tracks connected users for monitoring and debugging purposes.
 */
@Component
@Slf4j
public class WebSocketEventListener {

    // Track connected sessions (sessionId -> userId)
    private final Map<String, String> connectedSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();

        if (user != null) {
            String userId = extractUserId(user);
            connectedSessions.put(sessionId, userId);
            log.info("WebSocket connected - Session: {}, User: {}", sessionId, userId);
        } else {
            log.warn("WebSocket connected without authentication - Session: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = connectedSessions.remove(sessionId);

        if (userId != null) {
            log.info("WebSocket disconnected - Session: {}, User: {}", sessionId, userId);
        } else {
            log.info("WebSocket disconnected - Session: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        Principal user = headerAccessor.getUser();

        String userId = user != null ? extractUserId(user) : "anonymous";
        log.debug("WebSocket subscription - Session: {}, User: {}, Destination: {}", sessionId, userId, destination);
    }

    public int getConnectedUserCount() {
        return connectedSessions.size();
    }

    public boolean isUserConnected(String userId) {
        return connectedSessions.containsValue(userId);
    }

    private String extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            Object principalObj = authToken.getPrincipal();
            if (principalObj instanceof CustomUserDetails userDetails) {
                return userDetails.getId().toString();
            }
        }
        return principal.getName();
    }
}
