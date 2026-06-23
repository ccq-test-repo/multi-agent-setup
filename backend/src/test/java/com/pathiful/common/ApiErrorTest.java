package com.pathiful.common;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void shouldSetDefaultConstructor() {
        ApiError error = new ApiError();

        assertNotNull(error.getTimestamp());
        assertEquals(0, error.getStatus());
        assertNull(error.getError());
        assertNull(error.getMessage());
        assertNull(error.getPath());
    }

    @Test
    void shouldCreateApiErrorWithAllFields() {
        ApiError error = new ApiError(404, "NOT_FOUND", "Resource nicht gefunden.", "/api/test");

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("NOT_FOUND", error.getError());
        assertEquals("Resource nicht gefunden.", error.getMessage());
        assertEquals("/api/test", error.getPath());
    }

    @Test
    void shouldSetTimestampOnCreation() {
        LocalDateTime before = LocalDateTime.now();
        ApiError error = new ApiError(400, "BAD_REQUEST", "Fehler", "/path");
        LocalDateTime after = LocalDateTime.now();

        assertTrue(error.getTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(error.getTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    void shouldSupportAllErrorTypes() {
        ApiError badRequest = new ApiError(400, "BAD_REQUEST", "msg", "/path");
        ApiError unauthorized = new ApiError(401, "UNAUTHORIZED", "msg", "/path");
        ApiError forbidden = new ApiError(403, "FORBIDDEN", "msg", "/path");
        ApiError notFound = new ApiError(404, "NOT_FOUND", "msg", "/path");
        ApiError serverError = new ApiError(500, "INTERNAL_SERVER_ERROR", "msg", "/path");

        assertEquals(400, badRequest.getStatus());
        assertEquals(401, unauthorized.getStatus());
        assertEquals(403, forbidden.getStatus());
        assertEquals(404, notFound.getStatus());
        assertEquals(500, serverError.getStatus());
    }
}
