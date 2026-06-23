package com.pathiful.rating;

/**
 * Wird geworfen, wenn ein Nutzer versucht, eine Route mehrfach zu bewerten.
 */
public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(Long routeId, Long userId) {
        super("Route " + routeId + " wurde bereits von Nutzer " + userId + " bewertet.");
    }
}
