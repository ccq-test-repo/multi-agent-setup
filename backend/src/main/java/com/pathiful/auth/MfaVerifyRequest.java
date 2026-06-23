package com.pathiful.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-DTO für ADMIN-MFA-Verify.
 */
public class MfaVerifyRequest {

    @NotBlank(message = "TOTP-Code ist erforderlich")
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
