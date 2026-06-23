package com.pathiful.route;

import com.pathiful.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false, length = 20)
    private RouteType routeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, length = 20)
    private RouteTransportMode transportMode;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "elevation_gain_meters")
    private Integer elevationGainMeters;

    @Column(name = "scenic_score")
    private Integer scenicScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Route() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RouteType getRouteType() { return routeType; }
    public void setRouteType(RouteType routeType) { this.routeType = routeType; }
    public RouteTransportMode getTransportMode() { return transportMode; }
    public void setTransportMode(RouteTransportMode transportMode) { this.transportMode = transportMode; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getElevationGainMeters() { return elevationGainMeters; }
    public void setElevationGainMeters(Integer elevationGainMeters) { this.elevationGainMeters = elevationGainMeters; }
    public Integer getScenicScore() { return scenicScore; }
    public void setScenicScore(Integer scenicScore) { this.scenicScore = scenicScore; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum RouteType {
        ROUNDTRIP,
        DESTINATION
    }

    public enum RouteTransportMode {
        WALK,
        BIKE,
        CAR
    }

    public enum Visibility {
        PRIVATE,
        PUBLIC_LINK,
        PUBLIC
    }
}
