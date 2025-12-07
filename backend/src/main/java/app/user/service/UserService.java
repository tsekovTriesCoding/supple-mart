package app.user.service;

import app.config.CacheConfig;
import app.exception.ResourceNotFoundException;
import app.notification.event.AccountSecurityEvent;
import app.user.dto.RegisterRequest;
import app.user.dto.UpdateUserProfileRequest;
import app.user.mapper.UserMapper;
import app.user.model.AuthProvider;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for user management operations.
 * Does not implement UserDetailsService - authentication is handled by CustomUserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return user;
    }

    public User registerUser(RegisterRequest registerRequest) {
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = userMapper.toUser(registerRequest, encodedPassword);

        return userRepository.save(user);
    }

    /**
     * Find user by email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get user by ID - cached for repeated lookups.
     */
    @Cacheable(value = CacheConfig.USERS_CACHE, key = "#userId")
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Register a new OAuth2 user.
     */
    @Transactional
    public User registerOAuth2User(String email, String firstName, String lastName,
                                    String imageUrl, AuthProvider authProvider, String providerId) {
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .imageUrl(imageUrl)
                .authProvider(authProvider)
                .providerId(providerId)
                .role(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    /**
     * Update existing user with OAuth2 provider info (link accounts).
     */
    @Transactional
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#user.id")
    public User linkOAuth2Provider(User user, AuthProvider authProvider, String providerId, String imageUrl) {
        user.setAuthProvider(authProvider);
        user.setProviderId(providerId);
        user.setImageUrl(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update OAuth2 user profile from provider data.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#user.id")
    public User updateOAuth2User(User user, String firstName, String lastName, String imageUrl) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setImageUrl(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public Long getTotalUsersCount() {
        return userRepository.count();
    }

    public Page<User> getAllUsers(String search, String role, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Role roleEnum = (role != null && !role.isEmpty()) ? Role.valueOf(role) : null;

        return userRepository.findUsersWithSearch(search, roleEnum, pageable);
    }

    /**
     * Update user profile - evicts user cache.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#userId")
    public User updateUserProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        // Check if email is being changed and if it's already taken by another user
//        if (!user.getEmail().equals(request.getEmail())) {
//            userRepository.findByEmail(request.getEmail())
//                    .ifPresent(existingUser -> {
//                        if (!existingUser.getId().equals(userId)) {
//                            throw new BadRequestException("Email is already in use by another account");
//                        }
//                    });
//        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
//        user.setEmail(request.getEmail());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Change user password - evicts user cache.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#userId")
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        eventPublisher.publishEvent(new AccountSecurityEvent(
                this,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                "Password Changed",
                "Your password was successfully changed on " + LocalDateTime.now()
        ));
    }
}
