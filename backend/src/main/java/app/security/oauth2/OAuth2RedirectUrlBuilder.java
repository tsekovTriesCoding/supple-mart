package app.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Shared utility for OAuth2 redirect URL building.
 */
@Component
public class OAuth2RedirectUrlBuilder {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Get the base frontend URL with trailing slash removed.
     */
    public String getBaseUrl() {
        return frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    /**
     * Get the OAuth2 callback URL.
     */
    public String getCallbackUrl() {
        return getBaseUrl() + "/oauth2/callback";
    }
}

