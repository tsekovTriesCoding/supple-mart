package app.testutil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import app.security.CustomUserDetails;
import app.user.model.Role;
import app.user.model.User;

/**
 * Test utilities for JWT authentication in integration tests.
 */
public final class JwtTestUtils {

    private JwtTestUtils() {
        // Utility class
    }

    /**
     * Creates a CustomUserDetails for testing.
     */
    public static CustomUserDetails createUserDetails(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .isEnabled(true)
                .build();
    }

    /**
     * Creates a CustomUserDetails with specified values.
     */
    public static CustomUserDetails createUserDetails(UUID userId, String email, Role role) {
        return CustomUserDetails.builder()
                .id(userId)
                .username(email)
                .password("password")
                .role(role)
                .isEnabled(true)
                .build();
    }

    /**
     * Sets up SecurityContext with the given user details.
     */
    public static void setupSecurityContext(CustomUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Clears the security context.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Creates Authorization header value for given token.
     */
    public static String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * Custom annotation for mocking authenticated user in tests.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    public @interface WithMockCustomUser {
        String email() default "test@example.com";
        String role() default "CUSTOMER";
        String userId() default "";
    }

    /**
     * Security context factory for custom mock user.
     */
    @Component
    public static class WithMockCustomUserSecurityContextFactory
            implements WithSecurityContextFactory<WithMockCustomUser> {

        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            UUID userId = annotation.userId().isEmpty()
                    ? UUID.randomUUID()
                    : UUID.fromString(annotation.userId());

            Role role = Role.valueOf(annotation.role());

            CustomUserDetails userDetails = CustomUserDetails.builder()
                    .id(userId)
                    .username(annotation.email())
                    .password("password")
                    .role(role)
                    .isEnabled(true)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            context.setAuthentication(authentication);
            return context;
        }
    }
}

