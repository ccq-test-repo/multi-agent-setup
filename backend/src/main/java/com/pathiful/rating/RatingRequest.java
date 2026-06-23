package com.pathiful.rating;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request-DTO for POST /api/routes/{routeId}/ratings
 */
public class RatingRequest {

    @NotNull(message = "Sternebewertung (score) ist erforderlich")
    @Min(value = 1, message = "Mindestens 1 Stern")
    @Max(value = 5, message = "Maximal 5 Sterne")
    private Integer stars;

    private String comment;

    @JsonProperty("score")
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
