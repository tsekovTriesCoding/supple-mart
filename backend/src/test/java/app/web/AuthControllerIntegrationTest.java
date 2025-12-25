package app.web;

import app.BaseIntegrationTest;
import app.testutil.TestDataFactory;
import app.user.dto.LoginRequest;
import app.user.dto.RegisterRequest;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests authentication endpoints with a real database using Testcontainers.
 */
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    private static final String AUTH_BASE_URL = "/api/auth";

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setUp() {
        String uniqueEmail = TestDataFactory.generateUniqueEmail();
        existingUser = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Existing")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        existingUser = userRepository.save(existingUser);
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_WithValidData_ReturnsCreatedWithTokens() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("SecurePass123!")
                    .firstName("New")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.user.email").value(request.getEmail()))
                    .andExpect(jsonPath("$.user.firstName").value(request.getFirstName()))
                    .andExpect(jsonPath("$.user.lastName").value(request.getLastName()))
                    .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                    .andExpect(jsonPath("$.user.id").isNotEmpty());
        }

        @Test
        @DisplayName("Should fail registration with existing email")
        void register_WithExistingEmail_ReturnsBadRequest() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email(existingUser.getEmail()) // Using existing email
                    .password("SecurePass123!")
                    .firstName("Duplicate")
                    .lastName("User")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail registration with invalid email format")
        void register_WithInvalidEmail_ReturnsBadRequest() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("invalid-email")
                    .password("SecurePass123!")
                    .firstName("New")
                    .lastName("User")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail registration with short password")
        void register_WithShortPassword_ReturnsBadRequest() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("12345") // Less than 6 characters
                    .firstName("New")
                    .lastName("User")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail registration with missing required fields")
        void register_WithMissingFields_ReturnsBadRequest() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    // Missing password, firstName, lastName
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ReturnsOkWithTokens() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email(existingUser.getEmail())
                    .password("Password123!")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.user.email").value(existingUser.getEmail()))
                    .andExpect(jsonPath("$.user.id").isNotEmpty());
        }

        @Test
        @DisplayName("Should fail login with wrong password")
        void login_WithWrongPassword_ReturnsUnauthorized() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email(existingUser.getEmail())
                    .password("WrongPassword!")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should fail login with non-existent email")
        void login_WithNonExistentEmail_ReturnsUnauthorized() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("Password123!")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should fail login with invalid email format")
        void login_WithInvalidEmailFormat_ReturnsBadRequest() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("invalid-email")
                    .password("Password123!")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail login with empty credentials")
        void login_WithEmptyCredentials_ReturnsBadRequest() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("")
                    .password("")
                    .build();

            mockMvc.perform(post(AUTH_BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully with valid token")
        void logout_WithValidToken_ReturnsNoContent() throws Exception {
            String token = generateToken(existingUser);

            mockMvc.perform(post(AUTH_BASE_URL + "/logout")
                            .header("Authorization", bearerToken(token)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should handle logout without token gracefully")
        void logout_WithoutToken_ReturnsNoContent() throws Exception {
            mockMvc.perform(post(AUTH_BASE_URL + "/logout"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should handle logout with invalid header gracefully")
        void logout_WithInvalidHeader_ReturnsNoContent() throws Exception {
            mockMvc.perform(post(AUTH_BASE_URL + "/logout")
                            .header("Authorization", "InvalidToken"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidationTests {

        @Test
        @DisplayName("Should access protected endpoint with valid token")
        void protectedEndpoint_WithValidToken_ReturnsOk() throws Exception {
            String token = generateToken(existingUser);

            // Access a protected endpoint (e.g., getting the cart which requires auth)
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/cart")
                            .header("Authorization", bearerToken(token)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject access to protected endpoint without token")
        void protectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/cart"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
