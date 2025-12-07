package app.security.oauth2;

import app.security.CustomUserDetails;
import app.security.jwt.JwtService;
import app.user.dto.AuthResponse;
import app.user.mapper.UserMapper;
import app.user.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 authentication success handler.
 * Generates JWT tokens and redirects to frontend with tokens.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final OAuth2RedirectUrlBuilder urlBuilder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2UserPrincipal oAuth2User = (OAuth2UserPrincipal) authentication.getPrincipal();

        // Get user directly from principal - no database fetch needed
        User user = oAuth2User.getUser();

        // Create UserDetails for JWT generation using mapper
        CustomUserDetails userDetails = userMapper.toCustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("OAuth2 login successful for user: {}", user.getEmail());

        // Reuse UserMapper to build user info, then serialize to JSON
        AuthResponse.UserInfo userInfo = userMapper.toUserInfo(user);
        String encodedUserInfo = URLEncoder.encode(objectMapper.writeValueAsString(userInfo), StandardCharsets.UTF_8);

        // Redirect to frontend with tokens and user info
        String targetUrl = UriComponentsBuilder.fromUriString(urlBuilder.getCallbackUrl())
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("user", encodedUserInfo)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

