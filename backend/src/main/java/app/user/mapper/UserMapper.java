package app.user.mapper;

import app.security.CustomUserDetails;
import app.user.dto.AuthResponse;
import app.user.dto.RegisterRequest;
import app.user.dto.UserProfileResponse;
import app.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", source = "email")
    @Mapping(target = "isEnabled", constant = "true")
    CustomUserDetails toCustomUserDetails(User user);

    default AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserInfo(user))
                .build();
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "role")
    AuthResponse.UserInfo toUserInfo(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "email", source = "registerRequest.email")
    @Mapping(target = "firstName", source = "registerRequest.firstName")
    @Mapping(target = "lastName", source = "registerRequest.lastName")
    @Mapping(target = "role", source = "registerRequest.role")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    User toUser(RegisterRequest registerRequest, String encodedPassword);

    @Mapping(target = "id", expression = "java(user.getId().toString())")
    @Mapping(target = "name", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "createdAt", expression = "java(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)")
    UserProfileResponse toUserProfileResponse(User user);
}
