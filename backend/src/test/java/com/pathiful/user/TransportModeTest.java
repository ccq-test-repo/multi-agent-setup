package com.pathiful.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TransportModeTest {

    @Test
    void shouldHaveThreeModes() {
        TransportMode[] modes = TransportMode.values();
        assertEquals(3, modes.length);
    }

    @Test
    void shouldIncludeWalkBikeCar() {
        assertNotNull(TransportMode.valueOf("WALK"));
        assertNotNull(TransportMode.valueOf("BIKE"));
        assertNotNull(TransportMode.valueOf("CAR"));
    }
}
