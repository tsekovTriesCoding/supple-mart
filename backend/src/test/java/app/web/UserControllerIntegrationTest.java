package app.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.BaseIntegrationTest;
import app.testutil.TestDataFactory;
import app.user.dto.ChangePasswordRequest;
import app.user.dto.UpdateUserProfileRequest;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;

/**
 * Integration tests for UserController.
 * Tests user profile management endpoints with a real database using Testcontainers.
 */
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static final String USER_BASE_URL = "/api/user";

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;
    private String rawPassword = "Password123!";

    @BeforeEach
    void setUp() {
        String uniqueEmail = TestDataFactory.generateUniqueEmail();
        testUser = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode(rawPassword))
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
        authToken = generateToken(testUser);
    }

    @Nested
    @DisplayName("GET /api/user/profile")
    class GetProfileTests {

        @Test
        @DisplayName("Should return user profile successfully")
        void getProfile_AuthenticatedUser_ReturnsProfile() throws Exception {
            mockMvc.perform(get(USER_BASE_URL + "/profile")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void getProfile_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(USER_BASE_URL + "/profile")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void getProfile_InvalidToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(USER_BASE_URL + "/profile")
                            .header("Authorization", "Bearer invalid.token.here")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/user/profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void updateProfile_ValidData_ReturnsUpdatedProfile() throws Exception {
            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/profile")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"));
        }

        @Test
        @DisplayName("Should return 400 for missing first name")
        void updateProfile_MissingFirstName_ReturnsBadRequest() throws Exception {
            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .lastName("Name")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/profile")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing last name")
        void updateProfile_MissingLastName_ReturnsBadRequest() throws Exception {
            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .firstName("Test")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/profile")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for first name too short")
        void updateProfile_FirstNameTooShort_ReturnsBadRequest() throws Exception {
            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .firstName("A") // Less than 2 characters
                    .lastName("Name")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/profile")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for last name too short")
        void updateProfile_LastNameTooShort_ReturnsBadRequest() throws Exception {
            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .firstName("Test")
                    .lastName("N") // Less than 2 characters
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/profile")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void updateProfile_WithoutAuth_ReturnsUnauthorized() throws Exception {
            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/user/change-password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_ValidPasswords_ReturnsOk() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(rawPassword)
                    .newPassword("NewPassword456!")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/change-password")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 401 for incorrect current password")
        void changePassword_IncorrectCurrentPassword_ReturnsUnauthorized() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("WrongPassword!")
                    .newPassword("NewPassword456!")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/change-password")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing current password")
        void changePassword_MissingCurrentPassword_ReturnsBadRequest() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .newPassword("NewPassword456!")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/change-password")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing new password")
        void changePassword_MissingNewPassword_ReturnsBadRequest() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(rawPassword)
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/change-password")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for new password too short")
        void changePassword_NewPasswordTooShort_ReturnsBadRequest() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(rawPassword)
                    .newPassword("short") // Less than 6 characters
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/change-password")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void changePassword_WithoutAuth_ReturnsUnauthorized() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(rawPassword)
                    .newPassword("NewPassword456!")
                    .build();

            mockMvc.perform(put(USER_BASE_URL + "/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/user/profile-picture")
    class DeleteProfilePictureTests {

        @Test
        @DisplayName("Should delete profile picture successfully")
        void deleteProfilePicture_AuthenticatedUser_ReturnsOk() throws Exception {
            mockMvc.perform(delete(USER_BASE_URL + "/profile-picture")
                            .header("Authorization", bearerToken(authToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void deleteProfilePicture_WithoutAuth_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(USER_BASE_URL + "/profile-picture")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
