package com.pathiful.auth;

/**
 * Response-DTO für ADMIN-MFA-Setup.
 */
public class MfaSetupResponse {

    private String secret;
    private String uri;

    public MfaSetupResponse() {}

    public MfaSetupResponse(String secret, String uri) {
        this.secret = secret;
        this.uri = uri;
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
}
