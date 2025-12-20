package app.user.service;

import app.cloudinary.CloudinaryService;
import app.exception.ResourceNotFoundException;
import app.user.dto.RegisterRequest;
import app.user.dto.UpdateUserProfileRequest;
import app.user.mapper.UserMapper;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("authenticateUser Tests")
    class AuthenticateUserTests {

        @Test
        @DisplayName("Should authenticate user successfully with valid credentials")
        void authenticateUser_WithValidCredentials_ReturnsUser() {
            String email = "test@example.com";
            String password = "password123";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            User result = userService.authenticateUser(email, password);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(password, testUser.getPassword());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void authenticateUser_WithNonExistentEmail_ThrowsException() {
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.authenticateUser(email, "password"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(email);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when password is invalid")
        void authenticateUser_WithInvalidPassword_ThrowsException() {
            String email = "test@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> userService.authenticateUser(email, "wrongPassword"))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid password");
        }
    }

    @Nested
    @DisplayName("registerUser Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register new user successfully")
        void registerUser_WithValidRequest_ReturnsNewUser() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("password123");
            request.setFirstName("Jane");
            request.setLastName("Doe");

            User mappedUser = User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(Role.CUSTOMER)
                    .build();

            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
            when(userMapper.toUser(eq(request), anyString())).thenReturn(mappedUser);
            when(userRepository.save(any(User.class))).thenReturn(mappedUser);

            User result = userService.registerUser(request);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(request.getEmail());
            verify(passwordEncoder).encode(request.getPassword());
            verify(userMapper).toUser(request, "encodedPassword");
            verify(userRepository).save(mappedUser);
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found by ID")
        void getUserById_WithExistingId_ReturnsUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            User result = userService.getUserById(userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void getUserById_WithNonExistentId_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }
    }

    @Nested
    @DisplayName("findByEmail Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should return Optional with user when found")
        void findByEmail_WithExistingEmail_ReturnsOptionalWithUser() {
            String email = "test@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findByEmail(email);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should return empty Optional when user not found")
        void findByEmail_WithNonExistentEmail_ReturnsEmptyOptional() {
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            Optional<User> result = userService.findByEmail(email);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail Tests")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void existsByEmail_WithExistingEmail_ReturnsTrue() {
            String email = "test@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            boolean result = userService.existsByEmail(email);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void existsByEmail_WithNonExistentEmail_ReturnsFalse() {
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            boolean result = userService.existsByEmail(email);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("registerOAuth2User Tests")
    class RegisterOAuth2UserTests {

        @Test
        @DisplayName("Should register OAuth2 user successfully")
        void registerOAuth2User_WithValidData_ReturnsNewUser() {
            String email = "oauth@example.com";
            String firstName = "OAuth";
            String lastName = "User";
            String imageUrl = "https://example.com/image.jpg";
            AuthProvider provider = AuthProvider.GOOGLE;
            String providerId = "google-123";

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(UUID.randomUUID());
                return savedUser;
            });

            User result = userService.registerOAuth2User(email, firstName, lastName, imageUrl, provider, providerId);

            assertThat(result).isNotNull();
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(email);
            assertThat(capturedUser.getFirstName()).isEqualTo(firstName);
            assertThat(capturedUser.getLastName()).isEqualTo(lastName);
            assertThat(capturedUser.getImageUrl()).isEqualTo(imageUrl);
            assertThat(capturedUser.getAuthProvider()).isEqualTo(provider);
            assertThat(capturedUser.getProviderId()).isEqualTo(providerId);
            assertThat(capturedUser.getRole()).isEqualTo(Role.CUSTOMER);
        }
    }

    @Nested
    @DisplayName("linkOAuth2Provider Tests")
    class LinkOAuth2ProviderTests {

        @Test
        @DisplayName("Should link OAuth2 provider to existing user")
        void linkOAuth2Provider_WithExistingUser_UpdatesProvider() {
            AuthProvider provider = AuthProvider.GITHUB;
            String providerId = "github-456";
            String imageUrl = "https://github.com/image.jpg";
            testUser.setImageUrl(null); // User has no image - isCloudinaryUrl won't be called due to short-circuit

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.linkOAuth2Provider(testUser, provider, providerId, imageUrl);

            assertThat(testUser.getAuthProvider()).isEqualTo(provider);
            assertThat(testUser.getProviderId()).isEqualTo(providerId);
            assertThat(testUser.getImageUrl()).isEqualTo(imageUrl);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should not update image if user has Cloudinary image")
        void linkOAuth2Provider_WithCloudinaryImage_KeepsExistingImage() {
            String existingCloudinaryUrl = "https://res.cloudinary.com/test/image.jpg";
            testUser.setImageUrl(existingCloudinaryUrl);

            when(cloudinaryService.isCloudinaryUrl(existingCloudinaryUrl)).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.linkOAuth2Provider(testUser, AuthProvider.GOOGLE, "google-123", "https://new-image.jpg");

            assertThat(testUser.getImageUrl()).isEqualTo(existingCloudinaryUrl);
        }
    }

    @Nested
    @DisplayName("updateUserProfile Tests")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("Should update user profile successfully")
        void updateUserProfile_WithValidRequest_ReturnsUpdatedUser() {
            UpdateUserProfileRequest request = new UpdateUserProfileRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updateUserProfile(userId, request);

            assertThat(result).isNotNull();
            assertThat(testUser.getFirstName()).isEqualTo("Updated");
            assertThat(testUser.getLastName()).isEqualTo("Name");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void updateUserProfile_WithNonExistentUser_ThrowsException() {
            UUID nonExistentId = UUID.randomUUID();
            UpdateUserProfileRequest request = new UpdateUserProfileRequest();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserProfile(nonExistentId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully with correct current password")
        void changePassword_WithValidCurrentPassword_ChangesPassword() {
            String currentPassword = "oldPassword";
            String newPassword = "newPassword";

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.changePassword(userId, currentPassword, newPassword);

            assertThat(testUser.getPassword()).isEqualTo("newEncodedPassword");
            verify(eventPublisher).publishEvent(any());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException with incorrect current password")
        void changePassword_WithInvalidCurrentPassword_ThrowsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(userId, "wrongPassword", "newPassword"))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Current password is incorrect");
        }
    }

    @Nested
    @DisplayName("getAllUsers Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users")
        void getAllUsers_WithValidParams_ReturnsPagedUsers() {
            Page<User> userPage = new PageImpl<>(List.of(testUser));
            when(userRepository.findUsersWithSearch(any(), any(), any(Pageable.class))).thenReturn(userPage);

            Page<User> result = userService.getAllUsers(null, null, 0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).findUsersWithSearch(any(), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter users by role")
        void getAllUsers_WithRoleFilter_ReturnsFilteredUsers() {
            Page<User> userPage = new PageImpl<>(List.of(testUser));
            when(userRepository.findUsersWithSearch(any(), eq(Role.CUSTOMER), any(Pageable.class))).thenReturn(userPage);

            Page<User> result = userService.getAllUsers(null, "CUSTOMER", 0, 10);

            assertThat(result).isNotNull();
            verify(userRepository).findUsersWithSearch(any(), eq(Role.CUSTOMER), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getTotalUsersCount Tests")
    class GetTotalUsersCountTests {

        @Test
        @DisplayName("Should return total users count")
        void getTotalUsersCount_ReturnsCount() {
            when(userRepository.count()).thenReturn(100L);

            Long result = userService.getTotalUsersCount();

            assertThat(result).isEqualTo(100L);
            verify(userRepository).count();
        }
    }
}
