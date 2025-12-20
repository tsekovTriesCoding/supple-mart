package app.security;

import app.user.mapper.UserMapper;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private CustomUserDetails testUserDetails;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDetails = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .role(testUser.getRole())
                .isEnabled(true)
                .build();
    }

    @Nested
    @DisplayName("loadUserByUsername Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should return UserDetails when user is found")
        void loadUserByUsername_WithExistingUser_ReturnsUserDetails() {
            when(userService.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(userMapper.toCustomUserDetails(testUser)).thenReturn(testUserDetails);

            UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(testEmail);
            verify(userService).findByEmail(testEmail);
            verify(userMapper).toCustomUserDetails(testUser);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void loadUserByUsername_WithNonExistentUser_ThrowsException() {
            String nonExistentEmail = "nonexistent@example.com";
            when(userService.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nonExistentEmail))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(nonExistentEmail);

            verify(userService).findByEmail(nonExistentEmail);
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Should properly map user role to authorities")
        void loadUserByUsername_WithAdminUser_ReturnsCorrectAuthorities() {
            testUser.setRole(Role.ADMIN);
            CustomUserDetails adminUserDetails = CustomUserDetails.builder()
                    .id(testUser.getId())
                    .username(testUser.getEmail())
                    .password(testUser.getPassword())
                    .role(Role.ADMIN)
                    .isEnabled(true)
                    .build();

            when(userService.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(userMapper.toCustomUserDetails(testUser)).thenReturn(adminUserDetails);

            UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

            assertThat(result).isNotNull();
            assertThat(result.getAuthorities()).isNotEmpty();
        }
    }
}
