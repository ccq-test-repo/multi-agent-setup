package com.pathiful.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldCreateWithResourceAndId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", 42L);

        assertEquals("User mit ID 42 nicht gefunden.", ex.getMessage());
    }

    @Test
    void shouldCreateWithCustomMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Route nicht gefunden.");

        assertEquals("Route nicht gefunden.", ex.getMessage());
    }

    @Test
    void shouldBeARuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test", 1L);

        assertInstanceOf(RuntimeException.class, ex);
    }
}
