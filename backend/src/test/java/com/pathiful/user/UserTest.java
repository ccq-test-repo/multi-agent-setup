package com.pathiful.user;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldCreateUserWithConstructor() {
        User user = new User("test@example.com", "hashed-password", User.Role.USER);

        assertNull(user.getId(), "ID should be null before persisting");
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPasswordHash());
        assertEquals(User.Role.USER, user.getRole());
        assertFalse(user.isMfaEnabled(), "MFA should be disabled by default");
    }

    @Test
    void shouldCreateAdminUser() {
        User user = new User("admin@example.com", "admin-hash", User.Role.ADMIN);

        assertEquals(User.Role.ADMIN, user.getRole());
    }

    @Test
    void shouldSetAndGetProperties() {
        User user = new User();
        user.setId(42L);
        user.setEmail("updated@example.com");
        user.setPasswordHash("new-hash");
        user.setRole(User.Role.ADMIN);
        user.setMfaEnabled(true);

        assertEquals(42L, user.getId());
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(User.Role.ADMIN, user.getRole());
        assertTrue(user.isMfaEnabled());
    }

    @Test
    void shouldSetLastLoginAt() {
        User user = new User("test@example.com", "hash", User.Role.USER);
        LocalDateTime loginTime = LocalDateTime.of(2025, 6, 1, 12, 0);
        user.setLastLoginAt(loginTime);

        assertEquals(loginTime, user.getLastLoginAt());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        User user = new User();
        assertNull(user.getCreatedAt(), "createdAt should be null before @PrePersist");

        user.onCreate();

        assertNotNull(user.getCreatedAt(), "createdAt should be set after @PrePersist");
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void shouldDefaultMfaEnabledToFalse() {
        User user = new User("test@example.com", "hash", User.Role.USER);
        assertFalse(user.isMfaEnabled());
    }

    @Test
    void shouldAllowEnablingMfa() {
        User user = new User("test@example.com", "hash", User.Role.USER);
        user.setMfaEnabled(true);
        assertTrue(user.isMfaEnabled());
    }
}
