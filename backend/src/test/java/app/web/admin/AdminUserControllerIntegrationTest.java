package app.web.admin;

import app.BaseIntegrationTest;
import app.testutil.TestDataFactory;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminUserController.
 * Tests admin user management endpoints with a real database using Testcontainers.
 */
@DisplayName("Admin User Controller Integration Tests")
class AdminUserControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN_USERS_BASE_URL = "/api/admin/users";

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        String adminEmail = TestDataFactory.generateUniqueEmail();
        adminUser = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("AdminPass123!"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = generateToken(adminUser);

        String userEmail = TestDataFactory.generateUniqueEmail();
        regularUser = User.builder()
                .email(userEmail)
                .password(passwordEncoder.encode("UserPass123!"))
                .firstName("Regular")
                .lastName("Customer")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        regularUser = userRepository.save(regularUser);
        userToken = generateToken(regularUser);

        // Create additional users for pagination testing
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Test" + i)
                    .lastName("User" + i)
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users for admin")
        void getAllUsers_AsAdmin_ReturnsUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        @DisplayName("Should filter users by role")
        void getAllUsers_WithRoleFilter_ReturnsFilteredUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("role", "CUSTOMER")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter admin users by role")
        void getAllUsers_WithAdminRoleFilter_ReturnsAdmins() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("role", "ADMIN")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should search users by name")
        void getAllUsers_WithSearchTerm_ReturnsMatchingUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("search", "Regular")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should search users by email")
        void getAllUsers_SearchByEmail_ReturnsMatchingUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("search", regularUser.getEmail().substring(0, 5))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should paginate users")
        void getAllUsers_WithPagination_ReturnsPaginatedUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("page", "0")
                            .param("size", "2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))));
        }

        @Test
        @DisplayName("Should return second page of users")
        void getAllUsers_SecondPage_ReturnsDifferentUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("page", "1")
                            .param("size", "2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.currentPage").value(1));
        }

        @Test
        @DisplayName("Should return 403 for regular user")
        void getAllUsers_AsRegularUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(userToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getAllUsers_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void getAllUsers_InvalidToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", "Bearer invalid.token.here")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should combine search and role filters")
        void getAllUsers_WithSearchAndRoleFilters_ReturnsFilteredUsers() throws Exception {
            mockMvc.perform(get(ADMIN_USERS_BASE_URL)
                            .header("Authorization", bearerToken(adminToken))
                            .param("search", "Test")
                            .param("role", "CUSTOMER")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }
}
