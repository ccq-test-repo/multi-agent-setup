package com.pathiful.auth;

import com.pathiful.user.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-Service für die Authentifizierung.
 *
 * Generiert sichere Zufalls-Tokens (256 Bit Base64-url-encoded)
 * und verwaltet sie In-Memory (für den Prototypen).
 * In einer Produktionsumgebung durch JWT (mit Refresh-Token) ersetzt.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final int TOKEN_BYTES = 32;

    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();

    @PostConstruct
    void logWarning() {
        log.info("TokenService: In-Memory Token-Verwaltung aktiv (Prototyp)");
    }

    /**
     * Generiert ein neues Token für einen Benutzer.
     */
    public String createToken(User user) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        tokens.put(token, new TokenEntry(user.getId(), user.getEmail(), user.getRole().name()));
        return token;
    }

    /**
     * Validiert ein Token und gibt die hinterlegten Daten zurück.
     */
    public Optional<TokenEntry> validateToken(String token) {
        TokenEntry entry = tokens.get(token);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    /**
     * Entfernt ein Token (Logout).
     */
    public boolean revokeToken(String token) {
        return tokens.remove(token) != null;
    }

    /**
     * Entfernt alle Tokens eines Benutzers (falls nötig).
     */
    public void revokeAllForUser(Long userId) {
        tokens.entrySet().removeIf(e -> e.getValue().userId.equals(userId));
    }

    // -----------------------------------------------------------------------
    // TokenEntry
    // -----------------------------------------------------------------------

    public record TokenEntry(Long userId, String email, String role) {}
}
