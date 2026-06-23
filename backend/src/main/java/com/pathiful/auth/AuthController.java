package com.pathiful.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST-Controller für Authentifizierung.
 *
 * POST /api/auth/register        – Registrierung
 * POST /api/auth/login            – Login
 * POST /api/auth/logout           – Logout
 * POST /api/auth/admin/mfa/setup  – MFA-Setup (ADMIN)
 * POST /api/auth/admin/mfa/verify – MFA-Verify (ADMIN)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/mfa/setup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MfaSetupResponse> setupMfa(
            @AuthenticationPrincipal AuthUser authUser) {
        MfaSetupResponse response = authService.setupMfa(authUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/mfa/complete – Abschluss eines MFA-Challenge-Logins.
     * Öffentlicher Endpunkt (noch kein Token erforderlich).
     * Akzeptiert { sessionId, code } und gibt das AuthResponse mit Token zurück.
     */
    @PostMapping("/mfa/complete")
    public ResponseEntity<AuthResponse> completeMfaLogin(@Valid @RequestBody MfaVerifyRequest request) {
        AuthResponse response = authService.completeMfaLogin(request.getSessionId(), request.getCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/mfa/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> verifyMfa(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody MfaVerifyRequest request) {
        boolean valid = authService.verifyMfa(authUser.getId(), request.getCode());
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
