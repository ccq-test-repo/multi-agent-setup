package com.pathiful.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void shouldCreateProfileWithUser() {
        User user = new User("test@example.com", "hash", User.Role.USER);
        UserProfile profile = new UserProfile(user);

        assertNull(profile.getId());
        assertSame(user, profile.getUser());
    }

    @Test
    void shouldSetAndGetAllPreferences() {
        User user = new User();
        UserProfile profile = new UserProfile(user);

        profile.setDisplayName("Testfahrer");
        profile.setPreferredTransportMode(TransportMode.BIKE);
        profile.setDefaultRoundtripDistanceKm(25.5);
        profile.setPreferNature(true);
        profile.setAvoidTraffic(true);
        profile.setPreferWater(false);
        profile.setPreferForest(true);
        profile.setPreferViewpoints(false);

        assertEquals("Testfahrer", profile.getDisplayName());
        assertEquals(TransportMode.BIKE, profile.getPreferredTransportMode());
        assertEquals(25.5, profile.getDefaultRoundtripDistanceKm(), 0.001);
        assertTrue(profile.getPreferNature());
        assertTrue(profile.getAvoidTraffic());
        assertFalse(profile.getPreferWater());
        assertTrue(profile.getPreferForest());
        assertFalse(profile.getPreferViewpoints());
    }

    @Test
    void shouldDefaultPreferencesToNull() {
        User user = new User();
        UserProfile profile = new UserProfile(user);

        assertNull(profile.getDisplayName());
        assertNull(profile.getPreferredTransportMode());
        assertNull(profile.getDefaultRoundtripDistanceKm());
        assertNull(profile.getPreferNature());
        assertNull(profile.getAvoidTraffic());
        assertNull(profile.getPreferWater());
        assertNull(profile.getPreferForest());
        assertNull(profile.getPreferViewpoints());
    }

    @Test
    void shouldSetId() {
        User user = new User();
        UserProfile profile = new UserProfile(user);
        profile.setId(1L);

        assertEquals(1L, profile.getId());
    }
}
