package com.pathiful.route;

import com.pathiful.route.Route.RouteTransportMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client für die openrouteservice API (OSM-basiertes Routing).
 *
 * Dokumentation: https://openrouteservice.org/dev/#/api-docs/v2/directions/{profile}/json
 */
@Component
public class OrsRoutingService {

    private static final Logger log = LoggerFactory.getLogger(OrsRoutingService.class);

    private final RestTemplate restTemplate;

    @Value("${openrouteservice.api-key:}")
    private String orsApiKey;

    @Value("${openrouteservice.base-url:https://api.openrouteservice.org/v2}")
    private String orsBaseUrl;

    public OrsRoutingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Ruft die openrouteservice Directions API für eine Route auf.
     *
     * @param startLat Start-Breitengrad
     * @param startLon Start-Längengrad
     * @param destLat  Ziel-Breitengrad (null bei Rundroute)
     * @param destLon  Ziel-Längengrad (null bei Rundroute)
     * @param mode     Verkehrsmittel (WALK, BIKE, CAR)
     * @return Liste der Wegpunkte (jeweils als String im Format "POINT(lon lat)")
     */
    public List<String> fetchRoute(double startLat, double startLon,
                                   Double destLat, Double destLon,
                                   RouteTransportMode mode) {
        String profile = mapProfile(mode);

        // Bei Rundroute: Ziel = Start + minimaler Rundweg (ORS unterstützt Rundrouten nicht nativ)
        double endLat = (destLat != null) ? destLat : startLat;
        double endLon = (destLon != null) ? destLon : startLon;

        String url = orsBaseUrl + "/directions/" + profile + "/json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", orsApiKey);

        // Request-Body: Koordinaten als [lon, lat]
        Map<String, Object> body = Map.of(
                "coordinates", List.of(
                        List.of(startLon, startLat),
                        List.of(endLon, endLat)
                ),
                "format", "geojson",
                "instructions", false
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("Calling ORS directions API: profile={}, start={}/{}, end={}/{}",
                profile, startLat, startLon, endLat, endLon);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("ORS API returned non-success status: {}", response.getStatusCode());
                throw new RuntimeException("openrouteservice API error: " + response.getStatusCode());
            }

            return parseGeoJson(response.getBody());
        } catch (Exception e) {
            log.error("ORS API request failed: {}", e.getMessage());
            throw new RuntimeException("Fehler bei der openrouteservice-Anfrage: " + e.getMessage(), e);
        }
    }

    /**
     * Mappt das Verkehrsmittel auf das ORS-Profil.
     */
    private String mapProfile(RouteTransportMode mode) {
        return switch (mode) {
            case WALK -> "foot-walking";
            case BIKE -> "cycling-regular";
            case CAR -> "driving-car";
        };
    }

    /**
     * Parst das GeoJSON-Response der ORS Directions API und extrahiert die Wegpunkte.
     *
     * Erwartetes Format:
     * {
     *   "features": [{
     *     "geometry": {
     *       "coordinates": [[lon1,lat1],[lon2,lat2],...]
     *     }
     *   }]
     * }
     */
    @SuppressWarnings("unchecked")
    List<String> parseGeoJson(Map<String, Object> responseBody) {
        List<String> points = new ArrayList<>();

        List<Map<String, Object>> features = (List<Map<String, Object>>) responseBody.get("features");
        if (features == null || features.isEmpty()) {
            log.warn("ORS response enthält keine features");
            return points;
        }

        Map<String, Object> firstFeature = features.get(0);
        Map<String, Object> geometry = (Map<String, Object>) firstFeature.get("geometry");
        if (geometry == null) return points;

        List<List<Double>> coords = (List<List<Double>>) geometry.get("coordinates");
        if (coords == null) return points;

        for (int i = 0; i < coords.size(); i++) {
            List<Double> coord = coords.get(i);
            double lon = coord.get(0);
            double lat = coord.get(1);
            points.add(String.format("POINT(%f %f)", lon, lat));
        }

        log.debug("Parsed {} waypoints from ORS response", points.size());
        return points;
    }
}
