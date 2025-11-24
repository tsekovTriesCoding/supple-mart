package app.user.service;

import app.exception.ResourceNotFoundException;
import app.notification.event.AccountSecurityEvent;
import app.user.dto.RegisterRequest;
import app.user.dto.UpdateUserProfileRequest;
import app.user.mapper.UserMapper;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found."));

        return userMapper.toCustomUserDetails(user);
    }

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

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Long getTotalUsersCount() {
        return userRepository.count();
    }

    public Page<User> getAllUsers(String search, String role, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Role roleEnum = (role != null && !role.isEmpty()) ? Role.valueOf(role) : null;

        return userRepository.findUsersWithSearch(search, roleEnum, pageable);
    }

    @Transactional
    public User updateUserProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = getUserById(userId);

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

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

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
