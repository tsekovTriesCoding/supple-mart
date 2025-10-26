package app.web;

import app.security.CustomUserDetails;
import app.security.jwt.JwtService;
import app.user.dto.AuthResponse;
import app.user.dto.LoginRequest;
import app.user.dto.RegisterRequest;
import app.user.mapper.UserMapper;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Authentication attempt for email: {}", loginRequest.getEmail());

        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        CustomUserDetails userDetails = userMapper.toCustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        AuthResponse response = userMapper.toAuthResponse(user, accessToken, refreshToken);

        log.info("User authenticated successfully: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());

        User user = userService.registerUser(registerRequest);

        CustomUserDetails userDetails = userMapper.toCustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        AuthResponse response = userMapper.toAuthResponse(user, accessToken, refreshToken);

        log.info("User registered successfully: {}", user.getEmail());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            jwtService.revokeToken(token);
            log.info("User logged out successfully - token revoked");
        } else {
            log.info("Logout request without valid Authorization header - treating as idempotent success");
        }

        return ResponseEntity.noContent().build();
    }
}
