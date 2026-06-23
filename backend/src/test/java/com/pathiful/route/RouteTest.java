package com.pathiful.route;

import static org.junit.jupiter.api.Assertions.*;

import com.pathiful.user.User;
import org.junit.jupiter.api.Test;

class RouteTest {

    @Test
    void shouldSetAndGetProperties() {
        User owner = new User("owner@example.com", "hash", User.Role.USER);
        Route route = new Route();
        route.setOwner(owner);
        route.setName("Test-Route");
        route.setRouteType(Route.RouteType.ROUNDTRIP);
        route.setTransportMode(Route.RouteTransportMode.BIKE);
        route.setDistanceKm(15.3);
        route.setDurationMinutes(90);
        route.setElevationGainMeters(200);
        route.setScenicScore(8);
        route.setVisibility(Route.Visibility.PUBLIC);

        assertNull(route.getId());
        assertSame(owner, route.getOwner());
        assertEquals("Test-Route", route.getName());
        assertEquals(Route.RouteType.ROUNDTRIP, route.getRouteType());
        assertEquals(Route.RouteTransportMode.BIKE, route.getTransportMode());
        assertEquals(15.3, route.getDistanceKm(), 0.001);
        assertEquals(90, route.getDurationMinutes());
        assertEquals(200, route.getElevationGainMeters());
        assertEquals(8, route.getScenicScore());
        assertEquals(Route.Visibility.PUBLIC, route.getVisibility());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        Route route = new Route();
        assertNull(route.getCreatedAt());

        route.onCreate();

        assertNotNull(route.getCreatedAt());
    }

    @Test
    void shouldDefaultOptionalFieldsToNull() {
        Route route = new Route();
        assertNull(route.getDistanceKm());
        assertNull(route.getDurationMinutes());
        assertNull(route.getElevationGainMeters());
        assertNull(route.getScenicScore());
    }

    @Test
    void shouldSupportAllRouteTypes() {
        assertEquals(2, Route.RouteType.values().length);
        assertNotNull(Route.RouteType.valueOf("ROUNDTRIP"));
        assertNotNull(Route.RouteType.valueOf("DESTINATION"));
    }

    @Test
    void shouldSupportAllTransportModes() {
        assertEquals(3, Route.RouteTransportMode.values().length);
        assertNotNull(Route.RouteTransportMode.valueOf("WALK"));
        assertNotNull(Route.RouteTransportMode.valueOf("BIKE"));
        assertNotNull(Route.RouteTransportMode.valueOf("CAR"));
    }

    @Test
    void shouldSupportAllVisibilities() {
        assertEquals(3, Route.Visibility.values().length);
        assertNotNull(Route.Visibility.valueOf("PRIVATE"));
        assertNotNull(Route.Visibility.valueOf("PUBLIC_LINK"));
        assertNotNull(Route.Visibility.valueOf("PUBLIC"));
    }
}
