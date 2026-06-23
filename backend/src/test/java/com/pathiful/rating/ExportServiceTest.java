package com.pathiful.rating;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.route.Route;
import com.pathiful.route.RoutePoint;
import com.pathiful.route.RoutePointRepository;
import com.pathiful.route.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RoutePointRepository routePointRepository;

    private ExportService exportService;

    private Route route;

    private List<RoutePoint> points;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(routeRepository, routePointRepository);

        route = new Route();
        route.setId(1L);
        route.setName("Test-Route");
        route.setRouteType(Route.RouteType.ROUNDTRIP);
        route.setTransportMode(Route.RouteTransportMode.BIKE);
        route.setDistanceKm(15.0);

        RoutePoint p1 = new RoutePoint();
        p1.setId(1L);
        p1.setSequenceNumber(0);
        p1.setPointType(RoutePoint.PointType.START);
        p1.setCoordinates("POINT(11.582 48.135)");
        p1.setRecordedAt(LocalDateTime.of(2026, 6, 23, 10, 0));

        RoutePoint p2 = new RoutePoint();
        p2.setId(2L);
        p2.setSequenceNumber(1);
        p2.setPointType(RoutePoint.PointType.WAYPOINT);
        p2.setCoordinates("POINT(11.600 48.150)");
        p2.setRecordedAt(LocalDateTime.of(2026, 6, 23, 10, 15));

        RoutePoint p3 = new RoutePoint();
        p3.setId(3L);
        p3.setSequenceNumber(2);
        p3.setPointType(RoutePoint.PointType.DESTINATION);
        p3.setCoordinates("POINT(11.650 48.180)");
        p3.setRecordedAt(LocalDateTime.of(2026, 6, 23, 10, 30));

        points = List.of(p1, p2, p3);
    }

    // -----------------------------------------------------------------------
    // KML-Export
    // -----------------------------------------------------------------------

    @Test
    void shouldExportKmlSuccessfully() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routePointRepository.findByRouteIdOrderBySequenceNumber(1L)).thenReturn(points);

        String kml = exportService.exportAsKml(1L);

        assertThat(kml).isNotNull();
        assertThat(kml).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(kml).contains("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        assertThat(kml).contains("<name>Test-Route</name>");
        assertThat(kml).contains("<coordinates>11.582,48.135,0</coordinates>");
        assertThat(kml).contains("<coordinates>11.6,48.15,0</coordinates>");
        assertThat(kml).contains("<coordinates>11.65,48.18,0</coordinates>");
        assertThat(kml).contains("<LineString>");
        assertThat(kml).contains("</kml>");
    }

    @Test
    void shouldExportKmlWithXmlEscapedName() {
        route.setName("Route & Sonderzeichen <Test>");
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routePointRepository.findByRouteIdOrderBySequenceNumber(1L)).thenReturn(points);

        String kml = exportService.exportAsKml(1L);

        assertThat(kml).contains("<name>Route &amp; Sonderzeichen &lt;Test&gt;</name>");
    }

    @Test
    void shouldThrowWhenExportingNonexistentRouteAsKml() {
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportAsKml(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // -----------------------------------------------------------------------
    // GPX-Export
    // -----------------------------------------------------------------------

    @Test
    void shouldExportGpxSuccessfully() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routePointRepository.findByRouteIdOrderBySequenceNumber(1L)).thenReturn(points);

        String gpx = exportService.exportAsGpx(1L);

        assertThat(gpx).isNotNull();
        assertThat(gpx).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(gpx).contains("<gpx version=\"1.1\" creator=\"Pathiful\"");
        assertThat(gpx).contains("<name>Test-Route</name>");
        assertThat(gpx).contains("<wpt lat=\"48.135\" lon=\"11.582\">");
        assertThat(gpx).contains("<wpt lat=\"48.15\" lon=\"11.6\">");
        assertThat(gpx).contains("<wpt lat=\"48.18\" lon=\"11.65\">");
        assertThat(gpx).contains("<trkpt lat=\"48.135\" lon=\"11.582\">");
        assertThat(gpx).contains("</trk>");
        assertThat(gpx).contains("</gpx>");
    }

    @Test
    void shouldExportGpxWithTimeElement() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routePointRepository.findByRouteIdOrderBySequenceNumber(1L)).thenReturn(points);

        String gpx = exportService.exportAsGpx(1L);

        assertThat(gpx).contains("<time>2026-06-23T10:00:00Z</time>");
        assertThat(gpx).contains("<time>2026-06-23T10:15:00Z</time>");
    }

    @Test
    void shouldThrowWhenExportingNonexistentRouteAsGpx() {
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportAsGpx(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
