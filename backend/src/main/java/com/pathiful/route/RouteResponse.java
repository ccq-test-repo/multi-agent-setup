package com.pathiful.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response-DTO for route endpoints.
 * Feldnamen per @JsonProperty auf Frontend-Erwartungen gemappt.
 */
public class RouteResponse {

    private Long id;
    private String name;
    private String routeType;
    private String transportMode;
    private Double distanceKm;
    private Integer durationMinutes;
    private Integer elevationGainMeters;
    private Integer scenicScore;
    private String visibility;
    private List<RoutePointDto> points;
    private LocalDateTime createdAt;

    public RouteResponse() {}

    public static RouteResponse fromEntity(Route route, List<RoutePoint> points) {
        RouteResponse resp = new RouteResponse();
        resp.setId(route.getId());
        resp.setName(route.getName());
        resp.setRouteType(route.getRouteType().name());
        resp.setTransportMode(route.getTransportMode().name());
        resp.setDistanceKm(route.getDistanceKm());
        resp.setDurationMinutes(route.getDurationMinutes());
        resp.setElevationGainMeters(route.getElevationGainMeters());
        resp.setScenicScore(route.getScenicScore());
        resp.setVisibility(route.getVisibility().name());
        resp.setCreatedAt(route.getCreatedAt());
        resp.setPoints(points.stream()
                .map(RoutePointDto::fromEntity)
                .toList());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    @JsonProperty("type")
    public String getRouteType() { return routeType; }
    public void setRouteType(String routeType) { this.routeType = routeType; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    @JsonProperty("totalDistance")
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    @JsonProperty("totalDuration")
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getElevationGainMeters() { return elevationGainMeters; }
    public void setElevationGainMeters(Integer elevationGainMeters) { this.elevationGainMeters = elevationGainMeters; }
    @JsonProperty("sceneryScore")
    public Integer getScenicScore() { return scenicScore; }
    public void setScenicScore(Integer scenicScore) { this.scenicScore = scenicScore; }
    @JsonProperty("publicRoute")
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public List<RoutePointDto> getPoints() { return points; }
    public void setPoints(List<RoutePointDto> points) { this.points = points; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class RoutePointDto {
        private Integer sequenceNumber;
        private String pointType;
        private Double lat;
        private Double lon;

        public static RoutePointDto fromEntity(RoutePoint point) {
            RoutePointDto dto = new RoutePointDto();
            dto.setSequenceNumber(point.getSequenceNumber());
            dto.setPointType(point.getPointType().name());
            String coordStr = point.getCoordinates(); // "POINT(lon lat)"
            if (coordStr != null && coordStr.startsWith("POINT(")) {
                String inner = coordStr.substring(6, coordStr.length() - 1).trim();
                String[] parts = inner.split(" ");
                if (parts.length == 2) {
                    dto.setLon(Double.parseDouble(parts[0]));
                    dto.setLat(Double.parseDouble(parts[1]));
                }
            }
            return dto;
        }

        public Integer getSequenceNumber() { return sequenceNumber; }
        public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
        public String getPointType() { return pointType; }
        public void setPointType(String pointType) { this.pointType = pointType; }
        @JsonProperty("latitude")
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        @JsonProperty("longitude")
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
    }
}
