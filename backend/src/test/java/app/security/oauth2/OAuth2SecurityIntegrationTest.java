package app.security.oauth2;

import app.BaseIntegrationTest;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for OAuth2 authentication components.
 * Tests the CustomOAuth2UserService and related OAuth2 classes.
 */
class OAuth2SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    private User existingLocalUser;
    private User existingGoogleUser;

    @BeforeEach
    void setUp() {
        existingLocalUser = new User();
        existingLocalUser.setEmail("local-user-" + UUID.randomUUID() + "@example.com");
        existingLocalUser.setFirstName("Local");
        existingLocalUser.setLastName("User");
        existingLocalUser.setPassword("hashedpassword");
        existingLocalUser.setRole(Role.CUSTOMER);
        existingLocalUser.setAuthProvider(AuthProvider.LOCAL);
        existingLocalUser = userRepository.save(existingLocalUser);

        existingGoogleUser = new User();
        existingGoogleUser.setEmail("google-user-" + UUID.randomUUID() + "@gmail.com");
        existingGoogleUser.setFirstName("Google");
        existingGoogleUser.setLastName("User");
        existingGoogleUser.setRole(Role.CUSTOMER);
        existingGoogleUser.setAuthProvider(AuthProvider.GOOGLE);
        existingGoogleUser.setProviderId("google-id-12345");
        existingGoogleUser = userRepository.save(existingGoogleUser);
    }

    @Nested
    @DisplayName("OAuth2UserInfo Factory Tests")
    class OAuth2UserInfoFactoryTests {

        @Test
        @DisplayName("Should create GoogleOAuth2UserInfo for Google provider")
        void getOAuth2UserInfo_CreatesGoogleUserInfo() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "google-sub-123");
            attributes.put("name", "Test User");
            attributes.put("email", "test@gmail.com");
            attributes.put("picture", "https://example.com/photo.jpg");
            attributes.put("given_name", "Test");
            attributes.put("family_name", "User");

            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

            assertThat(userInfo).isInstanceOf(GoogleOAuth2UserInfo.class);
            assertThat(userInfo.getId()).isEqualTo("google-sub-123");
            assertThat(userInfo.getName()).isEqualTo("Test User");
            assertThat(userInfo.getEmail()).isEqualTo("test@gmail.com");
            assertThat(userInfo.getImageUrl()).isEqualTo("https://example.com/photo.jpg");
            assertThat(userInfo.getFirstName()).isEqualTo("Test");
            assertThat(userInfo.getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should create GitHubOAuth2UserInfo for GitHub provider")
        void getOAuth2UserInfo_CreatesGitHubUserInfo() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 12345);
            attributes.put("name", "GitHub User");
            attributes.put("email", "github@example.com");
            attributes.put("avatar_url", "https://github.com/avatar.jpg");
            attributes.put("login", "githubuser");

            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("github", attributes);

            assertThat(userInfo).isInstanceOf(GitHubOAuth2UserInfo.class);
            assertThat(userInfo.getId()).isEqualTo("12345");
            assertThat(userInfo.getEmail()).isEqualTo("github@example.com");
            assertThat(userInfo.getImageUrl()).isEqualTo("https://github.com/avatar.jpg");
        }

        @Test
        @DisplayName("Should throw exception for unsupported provider")
        void getOAuth2UserInfo_ThrowsForUnsupportedProvider() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", "123");
            attributes.put("email", "test@example.com");

            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo("facebook", attributes))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("OAuth2UserPrincipal Tests")
    class OAuth2UserPrincipalTests {

        @Test
        @DisplayName("Should create OAuth2UserPrincipal with correct authorities")
        void create_SetsCorrectAuthorities() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(Role.CUSTOMER);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "123");
            attributes.put("email", "test@example.com");

            OAuth2UserPrincipal principal = OAuth2UserPrincipal.create(user, attributes);

            assertThat(principal.getId()).isEqualTo(user.getId());
            assertThat(principal.getEmail()).isEqualTo("test@example.com");
            assertThat(principal.getUsername()).isEqualTo("test@example.com");
            assertThat(principal.getAuthorities()).hasSize(1);
            assertThat(principal.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("Should create OAuth2UserPrincipal with ADMIN role")
        void create_SetsAdminRole() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("admin@example.com");
            user.setFirstName("Admin");
            user.setLastName("User");
            user.setRole(Role.ADMIN);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "123");

            OAuth2UserPrincipal principal = OAuth2UserPrincipal.create(user, attributes);

            assertThat(principal.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should return user entity from principal")
        void getUser_ReturnsUserEntity() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(Role.CUSTOMER);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "123");

            OAuth2UserPrincipal principal = OAuth2UserPrincipal.create(user, attributes);

            assertThat(principal.getUser()).isEqualTo(user);
            assertThat(principal.getName()).isEqualTo(user.getId().toString());
        }

        @Test
        @DisplayName("Should preserve OAuth2 attributes")
        void getAttributes_PreservesOriginalAttributes() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setRole(Role.CUSTOMER);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "123");
            attributes.put("name", "Test User");
            attributes.put("picture", "https://example.com/photo.jpg");

            OAuth2UserPrincipal principal = OAuth2UserPrincipal.create(user, attributes);

            assertThat(principal.getAttributes()).containsAllEntriesOf(attributes);
        }
    }

    @Nested
    @DisplayName("Google OAuth2 User Info Tests")
    class GoogleOAuth2UserInfoTests {

        @Test
        @DisplayName("Should extract Google user info correctly")
        void googleOAuth2UserInfo_ExtractsAllFields() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "google-sub-12345");
            attributes.put("name", "John Doe");
            attributes.put("email", "john.doe@gmail.com");
            attributes.put("picture", "https://lh3.googleusercontent.com/photo.jpg");
            attributes.put("given_name", "John");
            attributes.put("family_name", "Doe");

            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            assertThat(userInfo.getId()).isEqualTo("google-sub-12345");
            assertThat(userInfo.getName()).isEqualTo("John Doe");
            assertThat(userInfo.getEmail()).isEqualTo("john.doe@gmail.com");
            assertThat(userInfo.getImageUrl()).isEqualTo("https://lh3.googleusercontent.com/photo.jpg");
            assertThat(userInfo.getFirstName()).isEqualTo("John");
            assertThat(userInfo.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should handle missing first and last name gracefully")
        void googleOAuth2UserInfo_HandlesEmptyNames() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "google-sub-123");
            attributes.put("name", "SingleName");
            attributes.put("email", "single@gmail.com");

            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            assertThat(userInfo.getId()).isEqualTo("google-sub-123");
            assertThat(userInfo.getName()).isEqualTo("SingleName");
            assertThat(userInfo.getFirstName()).isEqualTo("SingleName");
        }
    }

    @Nested
    @DisplayName("GitHub OAuth2 User Info Tests")
    class GitHubOAuth2UserInfoTests {

        @Test
        @DisplayName("Should extract GitHub user info correctly")
        void gitHubOAuth2UserInfo_ExtractsAllFields() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 12345678);
            attributes.put("name", "Jane Smith");
            attributes.put("email", "jane.smith@github.com");
            attributes.put("avatar_url", "https://avatars.githubusercontent.com/u/12345678");
            attributes.put("login", "janesmith");

            GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

            assertThat(userInfo.getId()).isEqualTo("12345678");
            assertThat(userInfo.getName()).isEqualTo("Jane Smith");
            assertThat(userInfo.getEmail()).isEqualTo("jane.smith@github.com");
            assertThat(userInfo.getImageUrl()).isEqualTo("https://avatars.githubusercontent.com/u/12345678");
        }

        @Test
        @DisplayName("Should use login as fallback name")
        void gitHubOAuth2UserInfo_UsesLoginAsFallback() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 12345);
            attributes.put("login", "developer123");
            attributes.put("email", "dev@example.com");
            // name is null

            GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

            assertThat(userInfo.getName()).isEqualTo("developer123");
        }
    }

    @Nested
    @DisplayName("User Registration via OAuth2 Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user via Google OAuth2")
        void registerOAuth2User_CreatesNewGoogleUser() {
            String email = "new-google-" + UUID.randomUUID() + "@gmail.com";
            String firstName = "New";
            String lastName = "GoogleUser";
            String imageUrl = "https://lh3.googleusercontent.com/photo.jpg";
            String providerId = "google-new-" + System.currentTimeMillis();

            User newUser = userService.registerOAuth2User(
                    email, firstName, lastName, imageUrl, AuthProvider.GOOGLE, providerId);

            assertThat(newUser).isNotNull();
            assertThat(newUser.getId()).isNotNull();
            assertThat(newUser.getEmail()).isEqualTo(email);
            assertThat(newUser.getFirstName()).isEqualTo(firstName);
            assertThat(newUser.getLastName()).isEqualTo(lastName);
            assertThat(newUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(newUser.getProviderId()).isEqualTo(providerId);
            assertThat(newUser.getImageUrl()).isEqualTo(imageUrl);
            assertThat(newUser.getPassword()).isNull(); // OAuth2 users don't have passwords
        }

        @Test
        @DisplayName("Should register new user via GitHub OAuth2")
        void registerOAuth2User_CreatesNewGitHubUser() {
            String email = "new-github-" + UUID.randomUUID() + "@github.com";
            String firstName = "New";
            String lastName = "GitHubUser";
            String imageUrl = "https://avatars.githubusercontent.com/u/123456";
            String providerId = "github-new-" + System.currentTimeMillis();

            User newUser = userService.registerOAuth2User(
                    email, firstName, lastName, imageUrl, AuthProvider.GITHUB, providerId);

            assertThat(newUser).isNotNull();
            assertThat(newUser.getEmail()).isEqualTo(email);
            assertThat(newUser.getAuthProvider()).isEqualTo(AuthProvider.GITHUB);
            assertThat(newUser.getProviderId()).isEqualTo(providerId);
        }
    }

    @Nested
    @DisplayName("OAuth2 Provider Linking Tests")
    class ProviderLinkingTests {

        @Test
        @DisplayName("Should link OAuth2 provider to existing local user")
        void linkOAuth2Provider_LinksToLocalUser() {
            String providerId = "google-link-" + System.currentTimeMillis();
            String imageUrl = "https://lh3.googleusercontent.com/newphoto.jpg";

            User linkedUser = userService.linkOAuth2Provider(
                    existingLocalUser, AuthProvider.GOOGLE, providerId, imageUrl);

            assertThat(linkedUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(linkedUser.getProviderId()).isEqualTo(providerId);
            assertThat(linkedUser.getImageUrl()).isEqualTo(imageUrl);
            // Password should still exist
            assertThat(linkedUser.getPassword()).isNotNull();
        }
    }

    @Nested
    @DisplayName("OAuth2 User Update Tests")
    class UserUpdateTests {

        @Test
        @DisplayName("Should update existing OAuth2 user info")
        void updateOAuth2User_UpdatesUserInfo() {
            String newFirstName = "UpdatedGoogle";
            String newLastName = "UpdatedUser";
            String newImageUrl = "https://lh3.googleusercontent.com/updated.jpg";

            User updatedUser = userService.updateOAuth2User(
                    existingGoogleUser, newFirstName, newLastName, newImageUrl);

            assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
            assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
            assertThat(updatedUser.getImageUrl()).isEqualTo(newImageUrl);
            // Email and provider should remain unchanged
            assertThat(updatedUser.getEmail()).isEqualTo(existingGoogleUser.getEmail());
            assertThat(updatedUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        }
    }
}
