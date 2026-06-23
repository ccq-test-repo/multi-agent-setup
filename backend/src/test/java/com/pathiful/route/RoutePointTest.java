package com.pathiful.route;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoutePointTest {

    @Test
    void shouldSetAndGetProperties() {
        Route route = new Route();
        RoutePoint point = new RoutePoint();
        point.setRoute(route);
        point.setSequenceNumber(1);
        point.setPointType(RoutePoint.PointType.START);
        point.setCoordinates("POINT(8.6821 50.1109)");

        assertNull(point.getId());
        assertSame(route, point.getRoute());
        assertEquals(1, point.getSequenceNumber());
        assertEquals(RoutePoint.PointType.START, point.getPointType());
        assertEquals("POINT(8.6821 50.1109)", point.getCoordinates());
    }

    @Test
    void shouldSupportAllPointTypes() {
        assertEquals(4, RoutePoint.PointType.values().length);
        assertNotNull(RoutePoint.PointType.valueOf("START"));
        assertNotNull(RoutePoint.PointType.valueOf("DESTINATION"));
        assertNotNull(RoutePoint.PointType.valueOf("WAYPOINT"));
        assertNotNull(RoutePoint.PointType.valueOf("TRACKING"));
    }

    @Test
    void shouldSetAndGetRecordedAt() {
        RoutePoint point = new RoutePoint();
        assertNull(point.getRecordedAt());

        var now = java.time.LocalDateTime.now();
        point.setRecordedAt(now);
        assertEquals(now, point.getRecordedAt());
    }
}
