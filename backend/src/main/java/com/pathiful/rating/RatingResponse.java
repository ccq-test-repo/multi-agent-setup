package com.pathiful.rating;

import java.time.LocalDateTime;

/**
 * Response-DTO for rating endpoints.
 */
public class RatingResponse {

    private Long id;
    private Long routeId;
    private Long userId;
    private Integer stars;
    private String comment;
    private LocalDateTime createdAt;

    public RatingResponse() {}

    public static RatingResponse fromEntity(RouteRating rating) {
        RatingResponse resp = new RatingResponse();
        resp.setId(rating.getId());
        resp.setRouteId(rating.getRoute().getId());
        resp.setUserId(rating.getUser().getId());
        resp.setStars(rating.getStars());
        resp.setComment(rating.getComment());
        resp.setCreatedAt(rating.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
