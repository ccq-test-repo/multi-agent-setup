package com.pathiful.feedback.model;

import java.time.Instant;

/**
 * Ein gespeicherter Feedback-Eintrag.
 * JSON-Feldnamen sind stabil ({@code id}, {@code title}, {@code description},
 * {@code category}, {@code createdAt}) und bilden den Vertrag für das Frontend.
 */
public class FeedbackEntry {

    private final Long id;
    private final String title;
    private final String description;
    private final FeedbackCategory category;
    private final Instant createdAt;

    public FeedbackEntry(Long id, String title, String description, FeedbackCategory category, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public FeedbackCategory getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
