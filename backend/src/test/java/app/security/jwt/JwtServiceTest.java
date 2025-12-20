package app.security.jwt;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtService jwtService;
    private SecretKey secretKey;
    private UserDetails testUserDetails;

    private static final String SECRET = "VGhpc0lzQVZlcnlTZWN1cmVTZWNyZXRLZXlGb3JUZXN0aW5nUHVycG9zZXNPbmx5MTIzNDU2Nzg5MA==";
    private static final long EXPIRATION = 3600L; // 1 hour in seconds
    private static final long REFRESH_EXPIRATION = 86400L; // 24 hours in seconds

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        jwtService = new JwtService(jwtConfig, secretKey);

        testUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .build();
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void generateToken_WithValidUserDetails_ReturnsToken() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);

            String token = jwtService.generateToken(testUserDetails);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
        }

        @Test
        @DisplayName("Should include username in token")
        void generateToken_WithUserDetails_ContainsUsername() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);

            String token = jwtService.generateToken(testUserDetails);
            String extractedUsername = jwtService.extractUsername(token);

            assertThat(extractedUsername).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("generateRefreshToken Tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate valid refresh token")
        void generateRefreshToken_WithValidUserDetails_ReturnsToken() {
            when(jwtConfig.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

            String refreshToken = jwtService.generateRefreshToken(testUserDetails);

            assertThat(refreshToken).isNotNull();
            assertThat(refreshToken).isNotEmpty();
        }

        @Test
        @DisplayName("Refresh token should have longer expiration than access token")
        void generateRefreshToken_ShouldHaveLongerExpiration() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            when(jwtConfig.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

            String accessToken = jwtService.generateToken(testUserDetails);
            String refreshToken = jwtService.generateRefreshToken(testUserDetails);

            Date accessExpiration = jwtService.extractExpiration(accessToken);
            Date refreshExpiration = jwtService.extractExpiration(refreshToken);

            assertThat(refreshExpiration).isAfter(accessExpiration);
        }
    }

    @Nested
    @DisplayName("extractUsername Tests")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should extract username from valid token")
        void extractUsername_FromValidToken_ReturnsUsername() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("extractExpiration Tests")
    class ExtractExpirationTests {

        @Test
        @DisplayName("Should extract expiration date from token")
        void extractExpiration_FromValidToken_ReturnsExpirationDate() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);

            Date expiration = jwtService.extractExpiration(token);

            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("isTokenValid Tests")
    class IsTokenValidTests {

        @Test
        @DisplayName("Should return true for valid token and matching user")
        void isTokenValid_WithValidTokenAndMatchingUser_ReturnsTrue() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);

            boolean isValid = jwtService.isTokenValid(token, testUserDetails);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false for valid token but different user")
        void isTokenValid_WithDifferentUser_ReturnsFalse() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);

            UserDetails differentUser = User.builder()
                    .username("different@example.com")
                    .password("password")
                    .authorities(Collections.emptyList())
                    .build();

            boolean isValid = jwtService.isTokenValid(token, differentUser);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for revoked token")
        void isTokenValid_WithRevokedToken_ReturnsFalse() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);
            jwtService.revokeToken(token);

            boolean isValid = jwtService.isTokenValid(token, testUserDetails);

            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("revokeToken Tests")
    class RevokeTokenTests {

        @Test
        @DisplayName("Should revoke token successfully")
        void revokeToken_WithValidToken_RevokesToken() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);

            jwtService.revokeToken(token);

            assertThat(jwtService.isTokenRevoked(token)).isTrue();
        }

        @Test
        @DisplayName("Should handle revocation of invalid token gracefully")
        void revokeToken_WithInvalidToken_HandlesGracefully() {
            String invalidToken = "invalid.token.here";

            jwtService.revokeToken(invalidToken);
            assertThat(jwtService.isTokenRevoked(invalidToken)).isTrue();
        }
    }

    @Nested
    @DisplayName("isTokenRevoked Tests")
    class IsTokenRevokedTests {

        @Test
        @DisplayName("Should return false for non-revoked token")
        void isTokenRevoked_WithNonRevokedToken_ReturnsFalse() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);

            boolean isRevoked = jwtService.isTokenRevoked(token);

            assertThat(isRevoked).isFalse();
        }

        @Test
        @DisplayName("Should return true for revoked token")
        void isTokenRevoked_WithRevokedToken_ReturnsTrue() {
            when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
            String token = jwtService.generateToken(testUserDetails);
            jwtService.revokeToken(token);

            boolean isRevoked = jwtService.isTokenRevoked(token);

            assertThat(isRevoked).isTrue();
        }
    }
}
