package com.pathiful.route;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.route.Route.RouteTransportMode;
import com.pathiful.route.Route.RouteType;
import com.pathiful.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service für Routenplanung und -verwaltung.
 *
 * Integration mit openrouteservice API für OSM-basiertes Routing.
 * Fallback bei fehlendem API-Key: Grobe Luftlinien-Schätzung.
 */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;
    private final RoutePointRepository routePointRepository;
    private final LandscapeScoringService scoringService;
    private final OrsRoutingService orsRoutingService;

    @Value("${openrouteservice.api-key:}")
    private String orsApiKey;

    public RouteService(RouteRepository routeRepository,
                        RoutePointRepository routePointRepository,
                        LandscapeScoringService scoringService,
                        OrsRoutingService orsRoutingService) {
        this.routeRepository = routeRepository;
        this.routePointRepository = routePointRepository;
        this.scoringService = scoringService;
        this.orsRoutingService = orsRoutingService;
    }

    /**
     * Erstellt eine Rundroute (ROUNDTRIP).
     *
     * @param request die Routen-Anfrage mit Startkoordinaten, Distanz und Verkehrsmittel
     * @param owner   der Besitzer der Route
     * @return RouteResponse mit allen Details und Wegpunkten
     */
    @Transactional
    public RouteResponse createRoundtrip(RouteRequest request, User owner) {
        validateRoundtrip(request);

        String name = (request.getName() != null && !request.getName().isBlank())
                ? request.getName()
                : "Rundtour " + LocalDateTime.now().toLocalDate();

        RouteTransportMode mode = parseTransportMode(request.getTransportMode());

        // Routing berechnen
        List<RoutePoint> points = calculateRoute(
                request.getStartLatDouble(),
                request.getStartLonDouble(),
                null, null,
                mode, RouteType.ROUNDTRIP,
                request.getDistanceKm()
        );

        // Scoring
        List<LandscapeScoringService.Waypoint> waypoints = points.stream()
                .map(p -> {
                    String coord = p.getCoordinates();
                    String inner = coord.substring(6, coord.length() - 1).trim();
                    String[] parts = inner.split(" ");
                    return new LandscapeScoringService.Waypoint(
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[0])
                    );
                })
                .toList();
        int scenicScore = scoringService.calculateScore(waypoints);

        // Route anlegen
        Route route = new Route();
        route.setOwner(owner);
        route.setName(name);
        route.setRouteType(RouteType.ROUNDTRIP);
        route.setTransportMode(mode);
        route.setDistanceKm(request.getDistanceKm());
        route.setDurationMinutes(estimateDuration(request.getDistanceKm(), mode));
        route.setScenicScore(scenicScore);
        route.setVisibility(Route.Visibility.PRIVATE);

        Route savedRoute = routeRepository.save(route);

        // Wegpunkte speichern
        for (RoutePoint point : points) {
            point.setRoute(savedRoute);
        }
        routePointRepository.saveAll(points);

        log.info("Roundtrip created: id={}, name='{}', distance={}km, score={}",
                savedRoute.getId(), name, savedRoute.getDistanceKm(), scenicScore);

        return RouteResponse.fromEntity(savedRoute, points);
    }

    /**
     * Erstellt eine Zielroute (DESTINATION).
     *
     * @param request die Routen-Anfrage mit Start-/Zielkoordinaten und Verkehrsmittel
     * @param owner   der Besitzer der Route
     * @return RouteResponse mit allen Details und Wegpunkten
     */
    @Transactional
    public RouteResponse createDestination(RouteRequest request, User owner) {
        validateDestination(request);

        if (!request.hasDestination()) {
            throw new IllegalArgumentException("Für Zielrouten (DESTINATION) müssen Start- und Zielkoordinaten angegeben werden.");
        }

        String name = (request.getName() != null && !request.getName().isBlank())
                ? request.getName()
                : "Zielroute " + LocalDateTime.now().toLocalDate();

        RouteTransportMode mode = parseTransportMode(request.getTransportMode());

        List<RoutePoint> points = calculateRoute(
                request.getStartLatDouble(),
                request.getStartLonDouble(),
                request.getDestLatDouble(),
                request.getDestLonDouble(),
                mode, RouteType.DESTINATION,
                null
        );

        // Distanz aus den Punkten schätzen
        double distanceKm = estimateDistanceFromPoints(points);

        // Scoring
        List<LandscapeScoringService.Waypoint> waypoints = points.stream()
                .map(p -> {
                    String coord = p.getCoordinates();
                    String inner = coord.substring(6, coord.length() - 1).trim();
                    String[] parts = inner.split(" ");
                    return new LandscapeScoringService.Waypoint(
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[0])
                    );
                })
                .toList();
        int scenicScore = scoringService.calculateScore(waypoints);

        Route route = new Route();
        route.setOwner(owner);
        route.setName(name);
        route.setRouteType(RouteType.DESTINATION);
        route.setTransportMode(mode);
        route.setDistanceKm(distanceKm);
        route.setDurationMinutes(estimateDuration(distanceKm, mode));
        route.setScenicScore(scenicScore);
        route.setVisibility(Route.Visibility.PRIVATE);

        Route savedRoute = routeRepository.save(route);

        for (RoutePoint point : points) {
            point.setRoute(savedRoute);
        }
        routePointRepository.saveAll(points);

        log.info("Destination route created: id={}, name='{}', distance={}km, score={}",
                savedRoute.getId(), name, distanceKm, scenicScore);

        return RouteResponse.fromEntity(savedRoute, points);
    }

    /**
     * Ruft eine gespeicherte Route ab.
     *
     * @param id   Routen-ID
     * @param user der angemeldete Benutzer
     * @return RouteResponse
     * @throws ResourceNotFoundException wenn die Route nicht existiert
     * @throws SecurityException         wenn der Benutzer nicht der Owner ist
     */
    @Transactional(readOnly = true)
    public RouteResponse getRoute(Long id, User user) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));

        if (!route.getOwner().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Zugriff verweigert: Sie sind nicht der Besitzer dieser Route.");
        }

        List<RoutePoint> points = routePointRepository.findByRouteIdOrderBySequenceNumber(id);

        return RouteResponse.fromEntity(route, points);
    }

    /**
     * Löscht eine gespeicherte Route.
     *
     * @param id   Routen-ID
     * @param user der angemeldete Benutzer
     * @throws ResourceNotFoundException wenn die Route nicht existiert
     * @throws SecurityException         wenn der Benutzer nicht der Owner ist
     */
    @Transactional
    public void deleteRoute(Long id, User user) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));

        if (!route.getOwner().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Zugriff verweigert: Sie sind nicht der Besitzer dieser Route.");
        }

        routePointRepository.deleteByRouteId(id);
        routeRepository.deleteById(id);
        log.info("Route deleted: id={}, by userId={}", id, user.getId());
    }

    // -----------------------------------------------------------------------
    // Validierung
    // -----------------------------------------------------------------------

    private void validateRoundtrip(RouteRequest request) {
        if (request.getDistanceKm() == null || request.getDistanceKm() <= 0) {
            throw new IllegalArgumentException("Für Rundrouten muss eine positive Distanz (distanceKm) angegeben werden.");
        }
    }

    private void validateDestination(RouteRequest request) {
        // Validierung erfolgt durch @Valid im Controller
    }

    private RouteTransportMode parseTransportMode(String mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Verkehrsmittel ist erforderlich.");
        }
        try {
            return RouteTransportMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nicht unterstütztes Verkehrsmittel: " + mode
                    + ". Erlaubt: WALK, BIKE, CAR");
        }
    }

    // -----------------------------------------------------------------------
    // Routing
    // -----------------------------------------------------------------------

    private List<RoutePoint> calculateRoute(double startLat, double startLon,
                                              Double destLat, Double destLon,
                                              RouteTransportMode mode,
                                              RouteType routeType,
                                              Double distanceKm) {
        if (orsApiKey != null && !orsApiKey.isEmpty() && !orsApiKey.isBlank()) {
            return calculateRouteWithOrs(startLat, startLon, destLat, destLon, mode);
        } else {
            log.info("Kein openrouteservice API-Key konfiguriert – verwende Luftlinien-Schätzung");
            return estimateRoute(startLat, startLon, destLat, destLon, routeType, distanceKm);
        }
    }

    private List<RoutePoint> calculateRouteWithOrs(double startLat, double startLon,
                                                     Double destLat, Double destLon,
                                                     RouteTransportMode mode) {
        log.info("Verwende openrouteservice für Routing: mode={}, start={}/{}",
                mode, startLat, startLon);

        List<String> orsPoints = orsRoutingService.fetchRoute(startLat, startLon, destLat, destLon, mode);

        if (orsPoints.isEmpty()) {
            log.warn("ORS lieferte keine Wegpunkte – verwende Fallback-Schätzung");
            return estimateRoute(startLat, startLon, destLat, destLon,
                    (destLat != null) ? RouteType.DESTINATION : RouteType.ROUNDTRIP,
                    10.0);
        }

        List<RoutePoint> points = new ArrayList<>();
        for (int i = 0; i < orsPoints.size(); i++) {
            RoutePoint.PointType pointType;
            if (i == 0) {
                pointType = RoutePoint.PointType.START;
            } else if (destLat != null && i == orsPoints.size() - 1) {
                pointType = RoutePoint.PointType.DESTINATION;
            } else {
                pointType = RoutePoint.PointType.WAYPOINT;
            }

            RoutePoint point = new RoutePoint();
            point.setSequenceNumber(i);
            point.setPointType(pointType);
            point.setCoordinates(orsPoints.get(i));
            point.setRecordedAt(LocalDateTime.now());
            points.add(point);
        }

        return points;
    }

    /**
     * Erzeugt geschätzte Wegpunkte (Luftlinie + Interpolation).
     */
    private List<RoutePoint> estimateRoute(double startLat, double startLon,
                                            Double destLat, Double destLon,
                                            RouteType routeType, Double distanceKm) {
        List<RoutePoint> points = new ArrayList<>();

        if (routeType == RouteType.DESTINATION && destLat != null && destLon != null) {
            // Zielroute: Start + interpolierte Zwischenpunkte + Ziel
            int segments = 10;
            double[] lats = interpolate(startLat, destLat, segments);
            double[] lons = interpolate(startLon, destLon, segments);

            for (int i = 0; i <= segments; i++) {
                RoutePoint.PointType pointType = (i == 0) ? RoutePoint.PointType.START
                        : (i == segments) ? RoutePoint.PointType.DESTINATION
                        : RoutePoint.PointType.WAYPOINT;

                points.add(createPoint(i, pointType, lons[i], lats[i]));
            }
        } else {
            // Rundroute: Kreis um Startkoordinate mit gegebenem Radius
            // Distanz in km → Umfang = distanceKm → Radius = distanceKm / (2*PI)
            double radiusDeg = (distanceKm / (2 * Math.PI)) / 111.0; // grobe Umrechnung km → Grad
            int numPoints = 16;

            for (int i = 0; i < numPoints; i++) {
                double angle = 2 * Math.PI * i / numPoints;
                double latDelta = radiusDeg * Math.cos(angle);
                double lonDelta = radiusDeg * Math.sin(angle) / Math.cos(Math.toRadians(startLat));

                RoutePoint.PointType pointType = (i == 0) ? RoutePoint.PointType.START
                        : RoutePoint.PointType.WAYPOINT;

                points.add(createPoint(i, pointType, startLon + lonDelta, startLat + latDelta));
            }
        }

        return points;
    }

    private RoutePoint createPoint(int seq, RoutePoint.PointType type, double lon, double lat) {
        RoutePoint point = new RoutePoint();
        point.setSequenceNumber(seq);
        point.setPointType(type);
        point.setCoordinates(String.format("POINT(%f %f)", lon, lat));
        point.setRecordedAt(LocalDateTime.now());
        return point;
    }

    private double[] interpolate(double from, double to, int segments) {
        double[] values = new double[segments + 1];
        for (int i = 0; i <= segments; i++) {
            values[i] = from + (to - from) * i / segments;
        }
        return values;
    }

    // -----------------------------------------------------------------------
    // Hilfsfunktionen
    // -----------------------------------------------------------------------

    private int estimateDuration(double distanceKm, RouteTransportMode mode) {
        // Durchschnittsgeschwindigkeit in km/h
        double speedKmh = switch (mode) {
            case WALK -> 5.0;
            case BIKE -> 15.0;
            case CAR -> 50.0;
        };
        return (int) Math.ceil((distanceKm / speedKmh) * 60);
    }

    private double estimateDistanceFromPoints(List<RoutePoint> points) {
        if (points.size() < 2) return 0;

        double totalKm = 0;
        for (int i = 1; i < points.size(); i++) {
            totalKm += haversineKm(
                    parseLat(points.get(i - 1).getCoordinates()),
                    parseLon(points.get(i - 1).getCoordinates()),
                    parseLat(points.get(i).getCoordinates()),
                    parseLon(points.get(i).getCoordinates())
            );
        }
        return Math.round(totalKm * 100.0) / 100.0;
    }

    private double parseLat(String coord) {
        return Double.parseDouble(coord.substring(6, coord.length() - 1).trim().split(" ")[1]);
    }

    private double parseLon(String coord) {
        return Double.parseDouble(coord.substring(6, coord.length() - 1).trim().split(" ")[0]);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
