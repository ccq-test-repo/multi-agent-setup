package com.pathiful.route;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LandscapeScoringServiceTest {

    private final LandscapeScoringService scoringService = new LandscapeScoringService();

    @Test
    void shouldReturnNeutralScoreForEmptyWaypoints() {
        int score = scoringService.calculateScore(List.of());
        assertThat(score).isBetween(0, 10);
        assertThat(score).isEqualTo(5);
    }

    @Test
    void shouldReturnHighScoreForAlpineRegion() {
        // Alpenregion (Zugspitze)
        var waypoints = List.of(
                new LandscapeScoringService.Waypoint(47.42, 10.98)
        );
        int score = scoringService.calculateScore(waypoints);
        assertThat(score).isBetween(0, 10);
        assertThat(score).isGreaterThanOrEqualTo(7);
    }

    @Test
    void shouldReturnHighScoreForBlackForest() {
        // Schwarzwald (Feldberg)
        var waypoints = List.of(
                new LandscapeScoringService.Waypoint(47.87, 8.00)
        );
        int score = scoringService.calculateScore(waypoints);
        assertThat(score).isGreaterThanOrEqualTo(7);
    }

    @Test
    void shouldReturnLowerScoreForMetropolitanAreas() {
        // Berlin Mitte
        var waypoints = List.of(
                new LandscapeScoringService.Waypoint(52.52, 13.40)
        );
        int score = scoringService.calculateScore(waypoints);
        assertThat(score).isLessThanOrEqualTo(4);
    }

    @Test
    void shouldAverageScoreAcrossMultipleWaypoints() {
        var waypoints = List.of(
                new LandscapeScoringService.Waypoint(52.52, 13.40),  // Berlin → 3
                new LandscapeScoringService.Waypoint(47.42, 10.98)   // Alpen → 8
        );
        int score = scoringService.calculateScore(waypoints);
        // (3 + 8) / 2 = 5.5 → 5 oder 6
        assertThat(score).isBetween(4, 7);
    }

    @Test
    void shouldNotExceedRangeBoundaries() {
        // Null-Daten: leere Liste liefert 5
        int score = scoringService.calculateScore(null);
        assertThat(score).isEqualTo(5);
    }

    @Test
    void shouldReturnScoreForLakeDistrict() {
        // Mecklenburgische Seenplatte
        var waypoints = List.of(
                new LandscapeScoringService.Waypoint(53.5, 12.5)
        );
        int score = scoringService.calculateScore(waypoints);
        assertThat(score).isGreaterThanOrEqualTo(6);
    }

    @Test
    void shouldReturnNeutralForUnknownRegions() {
        // Außereuropäische Koordinaten (z. B. Sydney)
        var waypoints = List.of(
                new LandscapeScoringService.Waypoint(-33.87, 151.21)
        );
        int score = scoringService.calculateScore(waypoints);
        assertThat(score).isEqualTo(5);
    }
}
