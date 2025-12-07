package app.security.oauth2;

import java.util.Map;

/**
 * Abstract class representing OAuth2 user information from various providers.
 * Each OAuth2 provider returns user info in different formats,
 * so we need provider-specific implementations.
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
    
    public String getFirstName() {
        String name = getName();
        if (name != null && name.contains(" ")) {
            return name.split(" ")[0];
        }
        return name != null ? name : "User";
    }
    
    public String getLastName() {
        String name = getName();
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ");
            return parts[parts.length - 1];
        }
        return "";
    }
}
