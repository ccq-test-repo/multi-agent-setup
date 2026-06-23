package com.pathiful.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Berechnet einen Landschafts-Score (0–10) für eine Route basierend auf
 * geografischen Kriterien:
 * - Nähe zu Grünflächen/Wäldern (forstwirtschaftliche Flächen anhand OSM-Daten)
 * - Nähe zu Wasserflächen (Seen, Flüsse)
 * - Vermeidung von stark befahrenen Straßen (anhand OSM highway-Typen)
 *
 * Im MVP wird eine vereinfachte heuristische Berechnung auf Basis der
 * Umgebungskoordinaten durchgeführt. Eine vollständige OSM-Overpass-Integration
 * ist für spätere Ausbaustufen vorgesehen.
 *
 * Score-Bereich:
 *  0–3  – geringe Landschaftsqualität (viel Verkehr, wenig Grün)
 *  4–6  – durchschnittlich
 *  7–10 – hohe Landschaftsqualität (viel Grün/Wasser)
 */
@Service
public class LandscapeScoringService {

    private static final Logger log = LoggerFactory.getLogger(LandscapeScoringService.class);

    /**
     * Berechnet den Landschafts-Score für eine Route anhand der RoutePoints.
     *
     * @param waypoints Liste der Wegpunkte (mindestens Start)
     * @return Score 0–10
     */
    public int calculateScore(java.util.List<Waypoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            return 5; // neutraler Default
        }

        // Vereinfachte Heuristik: basierend auf geografischer Breite und
        // angenommener Grün-/Wassernähe in typischen Outdoor-Regionen.
        // Später: OSM-Overpass-Query für tatsächliche Flächennutzung.

        double total = 0;
        int count = 0;

        for (Waypoint wp : waypoints) {
            total += scoreForCoordinate(wp.lat, wp.lon);
            count++;
        }

        int baseScore = (int) Math.round(total / count);
        return Math.max(0, Math.min(10, baseScore));
    }

    /**
     * Schätzt den Grün-/Wasseranteil für eine Koordinate auf Basis
     * bekannter geografischer Cluster:
     * - Mitteleuropa: Mittelgebirge, Alpen, Seen → tendenziell höher
     * - Städte / Flachland → tendenziell niedriger
     *
     * Im MVP als Platzhalter. Spätere API-Integration liefert reale Werte.
     */
    private int scoreForCoordinate(double lat, double lon) {
        // Grobe Heuristik – basiert auf bekannten Naturregionen
        // Alpenregion (höhere Scores)
        if (lat > 46 && lat < 48 && lon > 6 && lon < 12) {
            return 8; // Alpenvorland, Bergregion
        }
        // Mittelgebirge (Schwarzwald, Harz, Bayerischer Wald)
        if ((lat > 47 && lat < 49 && lon > 7 && lon < 9) ||   // Schwarzwald
            (lat > 51 && lat < 52 && lon > 10 && lon < 11) ||  // Harz
            (lat > 48 && lat < 50 && lon > 12 && lon < 14)) {  // Bayerischer Wald
            return 8;
        }
        // Seenplatte (z. B. Mecklenburgische Seenplatte)
        if (lat > 53 && lat < 54 && lon > 12 && lon < 14) {
            return 7;
        }
        // Großstädte (geringere Scores)
        if (isMetropolitanArea(lat, lon)) {
            return 3;
        }
        // Flachland / ländliche Gebiete – Durchschnitt
        if (lat > 49 && lat < 55 && lon > 6 && lon < 15) {
            return 5;
        }
        // Default für außereuropäische Koordinaten – neutral
        return 5;
    }

    private boolean isMetropolitanArea(double lat, double lon) {
        // Grobe Bounding-Boxes für bekannte Metropolregionen
        return (lat > 52.3 && lat < 52.7 && lon > 13.2 && lon < 13.6) ||  // Berlin
               (lat > 53.5 && lat < 53.7 && lon > 9.8 && lon < 10.1) ||   // Hamburg
               (lat > 48.0 && lat < 48.3 && lon > 11.3 && lon < 11.7) ||  // München
               (lat > 50.9 && lat < 51.1 && lon > 6.7 && lon < 7.1) ||    // Köln
               (lat > 50.0 && lat < 50.2 && lon > 8.5 && lon < 8.8);      // Frankfurt
    }

    /**
     * Interner Wegpunkt-Datentyp.
     */
    public record Waypoint(double lat, double lon) {}
}
