package com.pathiful.feedback.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request-DTO zum Erstellen eines Feedback-Eintrags (POST /feedback).
 * Feldnamen folgen dem Frontend-Vertrag (Issue #54/#55): title, description, category.
 *
 * Validierung:
 *  - title:       Pflichtfeld, max. 100 Zeichen (wie Frontend)
 *  - description: optional, max. 500 Zeichen (wie Frontend)
 *  - category:    Pflichtfeld, muss einer der Enum-Werte sein
 */
public class CreateFeedbackRequest {

    @NotBlank(message = "title must not be blank")
    @Size(max = 100, message = "title must not exceed 100 characters")
    private String title;

    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    @NotNull(message = "category must not be null")
    private FeedbackCategory category;

    public CreateFeedbackRequest() {
    }

    public CreateFeedbackRequest(String title, String description, FeedbackCategory category) {
        this.title = title;
        this.description = description;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FeedbackCategory getCategory() {
        return category;
    }

    public void setCategory(FeedbackCategory category) {
        this.category = category;
    }
}
