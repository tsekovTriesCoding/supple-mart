package app.web;

import app.security.CustomUserDetails;
import app.user.dto.ChangePasswordRequest;
import app.user.dto.UpdateUserProfileRequest;
import app.user.dto.UserProfileResponse;
import app.user.mapper.UserMapper;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Fetching profile for user: {}", userDetails.getUsername());

        User user = userService.getUserById(userDetails.getId());
        UserProfileResponse response = userMapper.toUserProfileResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        log.info("Updating profile for user: {}", userDetails.getUsername());

        User updatedUser = userService.updateUserProfile(userDetails.getId(), request);
        UserProfileResponse response = userMapper.toUserProfileResponse(updatedUser);

        log.info("Profile updated successfully for user: {}", updatedUser.getEmail());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("Password change requested for user: {}", userDetails.getUsername());

        userService.changePassword(
                userDetails.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        log.info("Password changed successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

