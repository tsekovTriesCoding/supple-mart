package app.web;

import app.security.CustomUserDetails;
import app.user.dto.ChangePasswordRequest;
import app.user.dto.UpdateUserProfileRequest;
import app.user.dto.UserProfileResponse;
import app.user.mapper.UserMapper;
import app.user.model.User;
import app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Get user profile", description = "Retrieve the current user's profile information")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Fetching profile for user: {}", userDetails.getUsername());

        User user = userService.getUserById(userDetails.getId());
        UserProfileResponse response = userMapper.toUserProfileResponse(user);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user profile", description = "Update the current user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid profile data")
    })
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

    @Operation(summary = "Change password", description = "Change the current user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password or passwords don't match")
    })
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

