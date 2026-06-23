package com.pathiful.auth;

import com.pathiful.user.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
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
    private static final int MFA_SESSION_TIMEOUT_SECONDS = 300; // 5 Minuten

    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();
    private final Map<String, MfaSession> mfaSessions = new ConcurrentHashMap<>();

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
    // MFA-Session
    // -----------------------------------------------------------------------

    /**
     * Erzeugt eine MFA-Session für einen Admin-Login.
     * Gültig für 5 Minuten.
     */
    public String createMfaSession(Long userId) {
        // Alte Sessions des Benutzers entfernen
        mfaSessions.entrySet().removeIf(e -> e.getValue().userId.equals(userId));

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        String sessionId = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        mfaSessions.put(sessionId, new MfaSession(
                userId,
                Instant.now().getEpochSecond(),
                MFA_SESSION_TIMEOUT_SECONDS));
        return sessionId;
    }

    /**
     * Validiert einen MFA-Code gegen eine Session.
     * Gibt die userId zurück oder wirft IllegalArgumentException.
     */
    public Long validateMfaSession(String sessionId, String code) {
        MfaSession session = mfaSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Ungültige MFA-Session.");
        }
        long now = Instant.now().getEpochSecond();
        if (now - session.createdAt > session.timeoutSeconds) {
            mfaSessions.remove(sessionId);
            throw new IllegalArgumentException("MFA-Session abgelaufen.");
        }

        // Simulierte TOTP-Prüfung
        boolean valid = "123456".equals(code) || "000000".equals(code);
        if (!valid) {
            throw new IllegalArgumentException("Ungültiger MFA-Code.");
        }

        mfaSessions.remove(sessionId);
        return session.userId;
    }

    // -----------------------------------------------------------------------
    // TokenEntry
    // -----------------------------------------------------------------------

    public record TokenEntry(Long userId, String email, String role) {}

    private record MfaSession(Long userId, long createdAt, long timeoutSeconds) {}
}
