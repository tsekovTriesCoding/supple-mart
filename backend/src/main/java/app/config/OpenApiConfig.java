package app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 * 
 * Access the documentation at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:SuppleMart API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. You can obtain it from the /api/auth/login endpoint.")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("SuppleMart API")
                .version("1.0.0")
                .description("""
                        SuppleMart E-Commerce REST API Documentation.
                        
                        ## Features
                        - **Authentication**: JWT-based authentication with login/register endpoints
                        - **Products**: Browse, search, and filter products
                        - **Cart**: Manage shopping cart items
                        - **Orders**: Place and track orders
                        - **Reviews**: Submit and manage product reviews
                        - **Wishlist**: Save products for later
                        - **Admin**: Product management, order management, user management
                        
                        ## Authentication
                        Most endpoints require authentication. Use the `/api/auth/login` endpoint to obtain a JWT token,
                        then click the "Authorize" button above and enter: `your-jwt-token`
                        """)
                .contact(new Contact()
                        .name("SuppleMart Support")
                        .email("support@supplemart.com")
                        .url("https://github.com/tsekovTriesCoding/supple-mart")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }
}
