package app.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing OAuth2 provider configuration.
 * Provides information about which OAuth2 providers are available.
 */
@Service
public class OAuth2ProviderService {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;

    /**
     * Returns a list of all configured OAuth2 providers.
     */
    public List<OAuth2ProviderInfo> getAvailableProviders() {
        List<OAuth2ProviderInfo> providers = new ArrayList<>();

        if (isProviderConfigured(googleClientId)) {
            providers.add(new OAuth2ProviderInfo("google", "Google", "/oauth2/authorization/google"));
        }

        if (isProviderConfigured(githubClientId)) {
            providers.add(new OAuth2ProviderInfo("github", "GitHub", "/oauth2/authorization/github"));
        }

        return providers;
    }

    /**
     * Checks if any OAuth2 provider is enabled.
     */
    public boolean isOAuth2Enabled() {
        return !getAvailableProviders().isEmpty();
    }

    private boolean isProviderConfigured(String clientId) {
        return clientId != null && !clientId.isEmpty();
    }

    /**
     * Record representing OAuth2 provider information.
     */
    public record OAuth2ProviderInfo(
            String name,
            String displayName,
            String authorizationUrl
    ) {}
}

