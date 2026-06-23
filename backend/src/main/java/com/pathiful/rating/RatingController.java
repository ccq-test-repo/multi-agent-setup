package com.pathiful.rating;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.route.RouteRepository;
import com.pathiful.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für Routen-Bewertungen.
 *
 * POST   /api/routes/{routeId}/ratings  – Bewertung abgeben
 * GET    /api/routes/{routeId}/ratings  – Bewertungen einer Route abrufen
 */
@RestController
@RequestMapping("/api/routes/{routeId}/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final RouteRepository routeRepository;

    public RatingController(RatingService ratingService, RouteRepository routeRepository) {
        this.ratingService = ratingService;
        this.routeRepository = routeRepository;
    }

    /**
     * POST /api/routes/{routeId}/ratings
     *
     * Erstellt eine Bewertung (1–5 Sterne, optionaler Kommentar).
     * Ein Nutzer kann jede Route nur einmal bewerten.
     */
    @PostMapping
    public ResponseEntity<RatingResponse> createRating(
            @PathVariable Long routeId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal User user) {

        RatingResponse response = ratingService.createRating(routeId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/routes/{routeId}/ratings
     *
     * Liefert alle Bewertungen für eine Route.
     */
    @GetMapping
    public ResponseEntity<List<RatingResponse>> getRatings(@PathVariable Long routeId) {
        List<RatingResponse> ratings = ratingService.getRatings(routeId);
        return ResponseEntity.ok(ratings);
    }
}
