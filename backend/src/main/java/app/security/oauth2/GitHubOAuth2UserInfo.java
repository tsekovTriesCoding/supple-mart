package app.security.oauth2;

import java.util.Map;

/**
 * GitHub OAuth2 user information extractor.
 * GitHub returns user info with specific attribute names.
 */
public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        // GitHub name can be null, fall back to login
        return name != null ? name : (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
    
    public String getLogin() {
        return (String) attributes.get("login");
    }
}
