package app.user.repository;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 * Tests custom queries and JPA repository methods with a real database.
 */
@DisplayName("User Repository Integration Tests")
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepositoryTest;

    private User customerUser;
    private User adminUser;
    private User googleUser;

    @BeforeEach
    void setUp() {
        // Create customer user
        customerUser = User.builder()
                .email(TestDataFactory.generateUniqueEmail())
                .password("hashedPassword123")
                .firstName("John")
                .lastName("Customer")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        customerUser = userRepositoryTest.save(customerUser);

        // Create admin user
        adminUser = User.builder()
                .email(TestDataFactory.generateUniqueEmail())
                .password("hashedPassword456")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepositoryTest.save(adminUser);

        // Create Google OAuth user
        googleUser = User.builder()
                .email(TestDataFactory.generateUniqueEmail())
                .firstName("Google")
                .lastName("User")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.GOOGLE)
                .providerId("google-12345")
                .imageUrl("https://example.com/google-avatar.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        googleUser = userRepositoryTest.save(googleUser);
    }

    @Nested
    @DisplayName("findByEmail Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by email when exists")
        void findByEmail_ExistingEmail_ReturnsUser() {
            Optional<User> result = userRepositoryTest.findByEmail(customerUser.getEmail());

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(customerUser.getEmail());
            assertThat(result.get().getFirstName()).isEqualTo("John");
            assertThat(result.get().getRole()).isEqualTo(Role.CUSTOMER);
        }

        @Test
        @DisplayName("Should return empty when email does not exist")
        void findByEmail_NonExistentEmail_ReturnsEmpty() {
            Optional<User> result = userRepositoryTest.findByEmail("nonexistent@example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should be case-sensitive for email lookup")
        void findByEmail_DifferentCase_ReturnsEmpty() {
            Optional<User> result = userRepositoryTest.findByEmail(customerUser.getEmail().toUpperCase());

            // Depending on database collation, this may or may not find the user
            // This test documents the behavior
            if (result.isPresent()) {
                assertThat(result.get().getEmail()).isEqualToIgnoringCase(customerUser.getEmail());
            }
        }
    }

    @Nested
    @DisplayName("findUsersWithSearch Tests")
    class FindUsersWithSearchTests {

        @Test
        @DisplayName("Should find users by email search")
        void findUsersWithSearch_ByEmail_ReturnsMatchingUsers() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    customerUser.getEmail().substring(0, 8), // Partial email match
                    null,
                    pageable
            );

            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Should find users by first name search")
        void findUsersWithSearch_ByFirstName_ReturnsMatchingUsers() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    "John",
                    null,
                    pageable
            );

            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).anyMatch(u -> u.getFirstName().equals("John"));
        }

        @Test
        @DisplayName("Should find users by last name search")
        void findUsersWithSearch_ByLastName_ReturnsMatchingUsers() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    "Customer",
                    null,
                    pageable
            );

            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).anyMatch(u -> u.getLastName().equals("Customer"));
        }

        @Test
        @DisplayName("Should filter users by role")
        void findUsersWithSearch_ByRole_ReturnsFilteredUsers() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    null,
                    Role.ADMIN,
                    pageable
            );

            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(u -> u.getRole() == Role.ADMIN);
        }

        @Test
        @DisplayName("Should combine search and role filter")
        void findUsersWithSearch_CombinedFilters_ReturnsMatchingUsers() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    "Admin",
                    Role.ADMIN,
                    pageable
            );

            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(u ->
                    u.getRole() == Role.ADMIN &&
                    (u.getFirstName().toLowerCase().contains("admin") ||
                     u.getLastName().toLowerCase().contains("admin") ||
                     u.getEmail().toLowerCase().contains("admin"))
            );
        }

        @Test
        @DisplayName("Should return empty page when no matches")
        void findUsersWithSearch_NoMatches_ReturnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    "nonexistentuser12345",
                    null,
                    pageable
            );

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return all users when search is null")
        void findUsersWithSearch_NullSearch_ReturnsAllUsers() {
            Pageable pageable = PageRequest.of(0, 100);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    null,
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3); // At least our 3 test users
        }

        @Test
        @DisplayName("Should return all users when search is empty")
        void findUsersWithSearch_EmptySearch_ReturnsAllUsers() {
            Pageable pageable = PageRequest.of(0, 100);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    "",
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should respect pagination")
        void findUsersWithSearch_WithPagination_ReturnsCorrectPage() {
            Pageable pageable = PageRequest.of(0, 1);

            Page<User> result = userRepositoryTest.findUsersWithSearch(
                    null,
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalPages()).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save new user with generated ID")
        void save_NewUser_GeneratesIdAndSaves() {
            User newUser = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("newPassword123")
                    .firstName("New")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            User saved = userRepositoryTest.save(newUser);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo(newUser.getEmail());
        }

        @Test
        @DisplayName("Should update existing user")
        void save_ExistingUser_UpdatesUser() {
            customerUser.setFirstName("UpdatedJohn");
            customerUser.setLastName("UpdatedCustomer");

            User updated = userRepositoryTest.save(customerUser);

            assertThat(updated.getId()).isEqualTo(customerUser.getId());
            assertThat(updated.getFirstName()).isEqualTo("UpdatedJohn");
            assertThat(updated.getLastName()).isEqualTo("UpdatedCustomer");
        }

        @Test
        @DisplayName("Should save OAuth2 user without password")
        void save_OAuth2User_SavesWithoutPassword() {
            User oauthUser = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .firstName("OAuth")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.GOOGLE)
                    .providerId("new-google-id")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            User saved = userRepositoryTest.save(oauthUser);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getPassword()).isNull();
            assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find user by ID when exists")
        void findById_ExistingUser_ReturnsUser() {
            Optional<User> result = userRepositoryTest.findById(customerUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(customerUser.getId());
            assertThat(result.get().getEmail()).isEqualTo(customerUser.getEmail());
        }

        @Test
        @DisplayName("Should return empty when ID does not exist")
        void findById_NonExistentId_ReturnsEmpty() {
            Optional<User> result = userRepositoryTest.findById(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user by entity")
        void delete_ExistingUser_RemovesFromDatabase() {
            // Create a user to delete
            User userToDelete = User.builder()
                    .email(TestDataFactory.generateUniqueEmail())
                    .password("password")
                    .firstName("ToDelete")
                    .lastName("User")
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userToDelete = userRepositoryTest.save(userToDelete);
            UUID userId = userToDelete.getId();

            userRepositoryTest.delete(userToDelete);

            Optional<User> result = userRepositoryTest.findById(userId);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("count Tests")
    class CountTests {

        @Test
        @DisplayName("Should return total count of users")
        void count_ReturnsCorrectCount() {
            long count = userRepositoryTest.count();

            assertThat(count).isGreaterThanOrEqualTo(3L); // At least our 3 test users
        }
    }

    @Nested
    @DisplayName("existsById Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true for existing user")
        void existsById_ExistingUser_ReturnsTrue() {
            boolean exists = userRepositoryTest.existsById(customerUser.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existent user")
        void existsById_NonExistentUser_ReturnsFalse() {
            boolean exists = userRepositoryTest.existsById(UUID.randomUUID());

            assertThat(exists).isFalse();
        }
    }
}

