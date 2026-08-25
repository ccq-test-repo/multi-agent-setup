package com.pathiful.feedback.model;

/**
 * Einheitliches Fehlerformat. {@code error} enthält eine verständliche Meldung,
 * keine internen Details oder Stacktraces (Security-Richtlinie).
 */
public class ErrorResponse {

    private String error;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
