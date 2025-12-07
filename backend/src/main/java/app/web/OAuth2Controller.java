package app.web;

import app.security.oauth2.OAuth2ProviderService;
import app.security.oauth2.OAuth2ProviderService.OAuth2ProviderInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for OAuth2 related endpoints.
 * Provides information about available OAuth2 providers to the frontend.
 */
@RestController
@RequestMapping("api/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "OAuth2 authentication endpoints")
public class OAuth2Controller {

    private final OAuth2ProviderService oAuth2ProviderService;

    @Operation(
            summary = "Get available OAuth2 providers",
            description = "Returns a list of configured OAuth2 providers"
    )
    @GetMapping("/providers")
    public ResponseEntity<OAuth2ProvidersResponse> getProviders() {
        List<OAuth2ProviderInfo> providers = oAuth2ProviderService.getAvailableProviders();
        return ResponseEntity.ok(new OAuth2ProvidersResponse(providers, oAuth2ProviderService.isOAuth2Enabled()));
    }

    /**
     * Response record for OAuth2 providers endpoint.
     */
    public record OAuth2ProvidersResponse(
            List<OAuth2ProviderInfo> providers,
            boolean enabled
    ) {}
}
