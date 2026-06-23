package com.pathiful.rating;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.route.Route;
import com.pathiful.route.RoutePoint;
import com.pathiful.route.RoutePointRepository;
import com.pathiful.route.RouteRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service zum Export von Routen im KML- und GPX-Format.
 */
@Service
public class ExportService {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final RouteRepository routeRepository;
    private final RoutePointRepository routePointRepository;

    public ExportService(RouteRepository routeRepository, RoutePointRepository routePointRepository) {
        this.routeRepository = routeRepository;
        this.routePointRepository = routePointRepository;
    }

    /**
     * Exportiert eine Route als KML.
     *
     * @param routeId ID der Route
     * @return KML-String
     */
    public String exportAsKml(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));
        List<RoutePoint> points = routePointRepository.findByRouteIdOrderBySequenceNumber(routeId);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        sb.append("  <Document>\n");
        sb.append("    <name>").append(escapeXml(route.getName())).append("</name>\n");
        sb.append("    <description>Exportiert von Pathiful</description>\n");

        // Placemark for each point
        for (RoutePoint point : points) {
            double lon = parseLon(point.getCoordinates());
            double lat = parseLat(point.getCoordinates());
            sb.append("    <Placemark>\n");
            sb.append("      <name>Punkt ").append(point.getSequenceNumber()).append("</name>\n");
            if (point.getPointType() != null) {
                sb.append("      <description>Typ: ").append(point.getPointType().name()).append("</description>\n");
            }
            sb.append("      <Point>\n");
            sb.append("        <coordinates>").append(lon).append(",").append(lat).append(",0</coordinates>\n");
            sb.append("      </Point>\n");
            sb.append("    </Placemark>\n");
        }

        // LineString for the route path
        sb.append("    <Placemark>\n");
        sb.append("      <name>Route</name>\n");
        sb.append("      <LineString>\n");
        sb.append("        <coordinates>\n");
        for (RoutePoint point : points) {
            double lon = parseLon(point.getCoordinates());
            double lat = parseLat(point.getCoordinates());
            sb.append("          ").append(lon).append(",").append(lat).append(",0\n");
        }
        sb.append("        </coordinates>\n");
        sb.append("      </LineString>\n");
        sb.append("    </Placemark>\n");

        sb.append("  </Document>\n");
        sb.append("</kml>\n");
        return sb.toString();
    }

    /**
     * Exportiert eine Route als GPX 1.1.
     *
     * @param routeId ID der Route
     * @return GPX-String
     */
    public String exportAsGpx(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));
        List<RoutePoint> points = routePointRepository.findByRouteIdOrderBySequenceNumber(routeId);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"Pathiful\"\n");
        sb.append("     xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        sb.append("  <metadata>\n");
        sb.append("    <name>").append(escapeXml(route.getName())).append("</name>\n");
        sb.append("    <desc>Exportiert von Pathiful</desc>\n");
        if (route.getCreatedAt() != null) {
            sb.append("    <time>").append(route.getCreatedAt().format(ISO_FMT)).append("</time>\n");
        }
        sb.append("  </metadata>\n");

        // Waypoints
        for (RoutePoint point : points) {
            double lon = parseLon(point.getCoordinates());
            double lat = parseLat(point.getCoordinates());
            sb.append("  <wpt lat=\"").append(lat).append("\" lon=\"").append(lon).append("\">\n");
            sb.append("    <name>Punkt ").append(point.getSequenceNumber()).append("</name>\n");
            if (point.getPointType() != null) {
                sb.append("    <desc>Typ: ").append(point.getPointType().name()).append("</desc>\n");
            }
            if (point.getRecordedAt() != null) {
                sb.append("    <time>").append(point.getRecordedAt().format(ISO_FMT)).append("</time>\n");
            }
            sb.append("  </wpt>\n");
        }

        // Track (trk)
        sb.append("  <trk>\n");
        sb.append("    <name>").append(escapeXml(route.getName())).append("</name>\n");
        sb.append("    <trkseg>\n");
        for (RoutePoint point : points) {
            double lon = parseLon(point.getCoordinates());
            double lat = parseLat(point.getCoordinates());
            sb.append("      <trkpt lat=\"").append(lat).append("\" lon=\"").append(lon).append("\">\n");
            if (point.getRecordedAt() != null) {
                sb.append("        <time>").append(point.getRecordedAt().format(ISO_FMT)).append("</time>\n");
            }
            sb.append("      </trkpt>\n");
        }
        sb.append("    </trkseg>\n");
        sb.append("  </trk>\n");

        sb.append("</gpx>\n");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Hilfsfunktionen
    // -----------------------------------------------------------------------

    private double parseLat(String coord) {
        return Double.parseDouble(coord.substring(6, coord.length() - 1).trim().split(" ")[1]);
    }

    private double parseLon(String coord) {
        return Double.parseDouble(coord.substring(6, coord.length() - 1).trim().split(" ")[0]);
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
