package app.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;
    private final SecretKey jwtSecretKey;

    // In-memory token revocation store with automatic cleanup
    // For production with multiple instances, consider Redis
    private final Map<String, Date> revokedTokens = new ConcurrentHashMap<>();

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtConfig.getExpiration() * 1000);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtConfig.getRefreshExpiration() * 1000);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(jwtSecretKey)
                .compact();
    }

    public void revokeToken(String token) {
        try {
            Date exp = extractExpiration(token);
            revokedTokens.put(token, exp);
            cleanupExpiredRevocations();
        } catch (RuntimeException e) {
            revokedTokens.put(token, new Date());
        }
    }

    private void cleanupExpiredRevocations() {
        Date now = new Date();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }

    /**
     * Scheduled cleanup of expired revoked tokens - runs every hour.
     * Prevents unbounded memory growth in long-running instances.
     */
    @Scheduled(fixedRate = 3600000)
    public void scheduledCleanup() {
        int sizeBefore = revokedTokens.size();
        cleanupExpiredRevocations();
        int removed = sizeBefore - revokedTokens.size();
        if (removed > 0) {
            log.debug("Cleaned up {} expired revoked tokens, {} remaining", removed, revokedTokens.size());
        }
    }

    public boolean isTokenRevoked(String token) {
        cleanupExpiredRevocations();
        return revokedTokens.containsKey(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (isTokenRevoked(token)) return false;
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}
