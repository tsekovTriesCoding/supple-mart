package app.security.oauth2;

import app.exception.OAuth2AuthenticationProcessingException;
import app.user.model.AuthProvider;
import app.user.model.User;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Custom OAuth2UserService that processes OAuth2 login and creates/updates users.
 * Extends DefaultOAuth2UserService to leverage Spring's built-in OAuth2 handling.
 * Delegates user operations to UserService for consistency and reuse.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (OAuth2AuthenticationProcessingException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationProcessingException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
        Optional<User> userOptional = userService.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                // Allow linking: user registered locally and now logs in via OAuth2
                user = userService.linkOAuth2Provider(user, authProvider,
                        oAuth2UserInfo.getId(), oAuth2UserInfo.getImageUrl());
                log.info("Linked OAuth2 provider {} to existing local user: {}", registrationId, user.getEmail());
            } else if (user.getAuthProvider() != authProvider) {
                throw new OAuth2AuthenticationProcessingException(
                        "You're already registered with " + user.getAuthProvider() + 
                        ". Please use your " + user.getAuthProvider() + " account to login.");
            } else {
                // Update existing OAuth2 user
                user = userService.updateOAuth2User(user, oAuth2UserInfo.getFirstName(),
                        oAuth2UserInfo.getLastName(), oAuth2UserInfo.getImageUrl());
            }
        } else {
            // Register new OAuth2 user
            user = userService.registerOAuth2User(
                    oAuth2UserInfo.getEmail(),
                    oAuth2UserInfo.getFirstName(),
                    oAuth2UserInfo.getLastName(),
                    oAuth2UserInfo.getImageUrl(),
                    authProvider,
                    oAuth2UserInfo.getId()
            );
            log.info("New OAuth2 user registered: {} via {}", user.getEmail(), registrationId);
        }

        return OAuth2UserPrincipal.create(user, oAuth2User.getAttributes());
    }
}
