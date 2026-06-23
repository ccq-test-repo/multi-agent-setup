package com.pathiful.route;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request-DTO for POST /api/routes/roundtrip and /api/routes/destination.
 */
public class RouteRequest {

    @NotBlank(message = "Startkoordinate (startLat) ist erforderlich")
    @DecimalMin(value = "-90", message = "startLat muss zwischen -90 und 90 liegen")
    @DecimalMax(value = "90", message = "startLat muss zwischen -90 und 90 liegen")
    private String startLat;

    @NotBlank(message = "Startkoordinate (startLon) ist erforderlich")
    @DecimalMin(value = "-180", message = "startLon muss zwischen -180 und 180 liegen")
    @DecimalMax(value = "180", message = "startLon muss zwischen -180 und 180 liegen")
    private String startLon;

    // Optional: nur für DESTINATION
    @DecimalMin(value = "-90", message = "destLat muss zwischen -90 und 90 liegen")
    @DecimalMax(value = "90", message = "destLat muss zwischen -90 und 90 liegen")
    private String destLat;

    @DecimalMin(value = "-180", message = "destLon muss zwischen -180 und 180 liegen")
    @DecimalMax(value = "180", message = "destLon muss zwischen -180 und 180 liegen")
    private String destLon;

    @NotBlank(message = "Verkehrsmittel ist erforderlich")
    private String transportMode; // WALK, BIKE, CAR

    // Optional: nur für ROUNDTRIP
    @Positive(message = "Distanz muss positiv sein")
    private Double distanceKm;

    private String name;

    // -- Getter / Setter --

    public String getStartLat() { return startLat; }
    public void setStartLat(String startLat) { this.startLat = startLat; }
    public String getStartLon() { return startLon; }
    public void setStartLon(String startLon) { this.startLon = startLon; }
    public String getDestLat() { return destLat; }
    public void setDestLat(String destLat) { this.destLat = destLat; }
    public String getDestLon() { return destLon; }
    public void setDestLon(String destLon) { this.destLon = destLon; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getStartLatDouble() { return Double.parseDouble(startLat); }
    public double getStartLonDouble() { return Double.parseDouble(startLon); }
    public boolean hasDestination() { return destLat != null && destLon != null; }
    public double getDestLatDouble() { return Double.parseDouble(destLat); }
    public double getDestLonDouble() { return Double.parseDouble(destLon); }
}
