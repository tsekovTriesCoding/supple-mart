package app.security.oauth2;

import app.exception.OAuth2AuthenticationProcessingException;
import app.user.model.AuthProvider;

import java.util.Map;

/**
 * Factory class to create appropriate OAuth2UserInfo based on the provider.
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case GITHUB -> new GitHubOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationProcessingException(
                    "Login with " + registrationId + " is not supported yet.");
        };
    }
}
