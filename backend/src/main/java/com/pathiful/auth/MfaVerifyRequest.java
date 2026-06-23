package com.pathiful.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-DTO für MFA-Verify / MFA-Challenge-Abschluss.
 */
public class MfaVerifyRequest {

    @NotBlank(message = "TOTP-Code ist erforderlich")
    private String code;

    private String sessionId;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
