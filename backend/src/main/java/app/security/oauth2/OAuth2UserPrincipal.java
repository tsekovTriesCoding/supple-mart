package app.security.oauth2;

import app.user.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Custom OAuth2User implementation that also implements UserDetails.
 * This allows us to use the same principal for both OAuth2 and form-based authentication.
 * Stores the full User entity to avoid additional database queries after authentication.
 */
@Getter
public class OAuth2UserPrincipal implements OAuth2User, UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public OAuth2UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        this.attributes = attributes;
    }

    public static OAuth2UserPrincipal create(User user, Map<String, Object> attributes) {
        return new OAuth2UserPrincipal(user, attributes);
    }

    public UUID getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }
}
