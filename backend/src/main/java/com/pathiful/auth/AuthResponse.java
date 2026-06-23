package com.pathiful.auth;

/**
 * Response-DTO für Login.
 * Enthält MFA-Challenge-Daten, wenn der Benutzer MFA aktiviert hat.
 */
public class AuthResponse {

    private Long userId;
    private String email;
    private String role;
    private String token;
    private boolean requiresMfa;
    private String mfaSessionId;

    public AuthResponse() {}

    public AuthResponse(Long userId, String email, String role, String token) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.token = token;
        this.requiresMfa = false;
        this.mfaSessionId = null;
    }

    /** Erzeugt eine MFA-Challenge-Response (noch kein Token). */
    public static AuthResponse mfaChallenge(Long userId, String email, String role, String sessionId) {
        AuthResponse resp = new AuthResponse();
        resp.userId = userId;
        resp.email = email;
        resp.role = role;
        resp.token = null;
        resp.requiresMfa = true;
        resp.mfaSessionId = sessionId;
        return resp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public boolean isRequiresMfa() { return requiresMfa; }
    public void setRequiresMfa(boolean requiresMfa) { this.requiresMfa = requiresMfa; }
    public String getMfaSessionId() { return mfaSessionId; }
    public void setMfaSessionId(String mfaSessionId) { this.mfaSessionId = mfaSessionId; }
}
