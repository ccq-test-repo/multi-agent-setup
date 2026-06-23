package com.pathiful.route;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * POST /api/routes/roundtrip
     *
     * Erstellt eine Rundroute basierend auf Startkoordinate, Distanz und Verkehrsmittel.
     */
    @PostMapping("/roundtrip")
    public ResponseEntity<RouteResponse> createRoundtrip(
            @Valid @RequestBody RouteRequest request,
            @AuthenticationPrincipal User user) {

        RouteResponse response = routeService.createRoundtrip(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/routes/destination
     *
     * Erstellt eine Zielroute basierend auf Start-/Zielkoordinate und Verkehrsmittel.
     */
    @PostMapping("/destination")
    public ResponseEntity<RouteResponse> createDestination(
            @Valid @RequestBody RouteRequest request,
            @AuthenticationPrincipal User user) {

        RouteResponse response = routeService.createDestination(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/routes/{id}
     *
     * Ruft eine gespeicherte Route mit allen Wegpunkten ab.
     * Nur der Besitzer (OWNER) oder Admin darf die Route abrufen.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        RouteResponse response = routeService.getRoute(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/routes/{id}
     *
     * Löscht eine gespeicherte Route und ihre Wegpunkte.
     * Nur der Besitzer (OWNER) oder Admin darf die Route löschen.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        routeService.deleteRoute(id, user);
        return ResponseEntity.noContent().build();
    }
}
