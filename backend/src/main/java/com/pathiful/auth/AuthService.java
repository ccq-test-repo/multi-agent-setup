package com.pathiful.auth;

import com.pathiful.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service für Registrierung, Login, Logout und ADMIN-MFA.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Registriert einen neuen Benutzer.
     *
     * @param request Registrierungsdaten
     * @return AuthResponse mit Token
     * @throws IllegalArgumentException bei doppelter E-Mail
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("E-Mail " + request.getEmail() + " ist bereits registriert.");
        }

        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);

        User saved = userRepository.save(user);

        String token = tokenService.createToken(saved);

        log.info("User registered: id={}, email={}", saved.getId(), saved.getEmail());

        return new AuthResponse(saved.getId(), saved.getEmail(), saved.getRole().name(), token);
    }

    /**
     * Meldet einen Benutzer an.
     *
     * @param request Login-Daten
     * @return AuthResponse mit Token
     * @throws IllegalArgumentException bei ungültigen Credentials
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Anmeldedaten."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Ungültige Anmeldedaten.");
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String token = tokenService.createToken(user);

        log.info("User logged in: id={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(user.getId(), user.getEmail(), user.getRole().name(), token);
    }

    /**
     * Meldet einen Benutzer ab (Token wird ungültig).
     */
    public void logout(String token) {
        if (token != null) {
            tokenService.revokeToken(token);
        }
    }

    /**
     * ADMIN: Aktiviert MFA für einen Benutzer.
     * Simuliert TOTP-Setup (kein echter TOTP-Generator).
     * In Produktion würde hier ein TOTP-Secret generiert und als QR-Code ausgeliefert.
     */
    @Transactional
    public MfaSetupResponse setupMfa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden."));

        if (user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("MFA ist nur für ADMIN-Benutzer verfügbar.");
        }

        // Simulierter TOTP-Secret (16 Base32-Zeichen)
        String simulatedSecret = "JBSWY3DPEHPK3PXP";
        String simulatedUri = "otpauth://totp/Pathiful:" + user.getEmail()
                + "?secret=" + simulatedSecret + "&issuer=Pathiful";

        user.setMfaEnabled(true);
        userRepository.save(user);

        log.info("MFA setup for admin: userId={}", userId);

        return new MfaSetupResponse(simulatedSecret, simulatedUri);
    }

    /**
     * ADMIN: Überprüft einen MFA-Code (simuliert).
     */
    public boolean verifyMfa(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden."));

        if (!user.isMfaEnabled()) {
            throw new IllegalArgumentException("MFA ist für diesen Benutzer nicht aktiviert.");
        }
        if (user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("MFA ist nur für ADMIN-Benutzer verfügbar.");
        }

        // Simulierte TOTP-Prüfung: Code "123456" oder "000000" akzeptieren
        // In Produktion: TOTP-Prüfung mit dem gespeicherten Secret
        boolean valid = "123456".equals(code) || "000000".equals(code);
        log.info("MFA verification for admin {}: {}", userId, valid ? "OK" : "FAILED");
        return valid;
    }
}
