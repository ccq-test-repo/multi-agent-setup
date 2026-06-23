package com.pathiful.user;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_transport_mode", length = 20)
    private TransportMode preferredTransportMode;

    @Column(name = "default_roundtrip_distance_km")
    private Double defaultRoundtripDistanceKm;

    @Column(name = "prefer_nature")
    private Boolean preferNature;

    @Column(name = "avoid_traffic")
    private Boolean avoidTraffic;

    @Column(name = "prefer_water")
    private Boolean preferWater;

    @Column(name = "prefer_forest")
    private Boolean preferForest;

    @Column(name = "prefer_viewpoints")
    private Boolean preferViewpoints;

    public UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public TransportMode getPreferredTransportMode() { return preferredTransportMode; }
    public void setPreferredTransportMode(TransportMode preferredTransportMode) { this.preferredTransportMode = preferredTransportMode; }
    public Double getDefaultRoundtripDistanceKm() { return defaultRoundtripDistanceKm; }
    public void setDefaultRoundtripDistanceKm(Double defaultRoundtripDistanceKm) { this.defaultRoundtripDistanceKm = defaultRoundtripDistanceKm; }
    public Boolean getPreferNature() { return preferNature; }
    public void setPreferNature(Boolean preferNature) { this.preferNature = preferNature; }
    public Boolean getAvoidTraffic() { return avoidTraffic; }
    public void setAvoidTraffic(Boolean avoidTraffic) { this.avoidTraffic = avoidTraffic; }
    public Boolean getPreferWater() { return preferWater; }
    public void setPreferWater(Boolean preferWater) { this.preferWater = preferWater; }
    public Boolean getPreferForest() { return preferForest; }
    public void setPreferForest(Boolean preferForest) { this.preferForest = preferForest; }
    public Boolean getPreferViewpoints() { return preferViewpoints; }
    public void setPreferViewpoints(Boolean preferViewpoints) { this.preferViewpoints = preferViewpoints; }
}
