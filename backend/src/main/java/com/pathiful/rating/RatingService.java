package com.pathiful.rating;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.route.Route;
import com.pathiful.route.RoutePoint;
import com.pathiful.route.RoutePointRepository;
import com.pathiful.route.RouteRepository;
import com.pathiful.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service zur Verwaltung von Routen-Bewertungen.
 */
@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RouteRatingRepository ratingRepository;
    private final RouteRepository routeRepository;
    private final RoutePointRepository routePointRepository;

    public RatingService(RouteRatingRepository ratingRepository,
                         RouteRepository routeRepository,
                         RoutePointRepository routePointRepository) {
        this.ratingRepository = ratingRepository;
        this.routeRepository = routeRepository;
        this.routePointRepository = routePointRepository;
    }

    /**
     * Erstellt eine Bewertung für eine Route.
     * Ein Nutzer kann jede Route nur einmal bewerten.
     *
     * @param routeId ID der zu bewertenden Route
     * @param user    der bewertende Nutzer
     * @param request Bewertungsdaten (Sterne + optionaler Kommentar)
     * @return RatingResponse
     * @throws ResourceNotFoundException wenn die Route nicht existiert
     * @throws DuplicateRatingException  wenn der Nutzer die Route bereits bewertet hat
     */
    @Transactional
    public RatingResponse createRating(Long routeId, User user, RatingRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));

        Optional<RouteRating> existing = ratingRepository.findByRouteIdAndUserId(routeId, user.getId());
        if (existing.isPresent()) {
            throw new DuplicateRatingException(routeId, user.getId());
        }

        RouteRating rating = new RouteRating();
        rating.setRoute(route);
        rating.setUser(user);
        rating.setStars(request.getStars());
        rating.setComment(request.getComment());

        RouteRating saved = ratingRepository.save(rating);

        log.info("Rating created: routeId={}, userId={}, stars={}", routeId, user.getId(), request.getStars());

        return RatingResponse.fromEntity(saved);
    }

    /**
     * Liefert alle Bewertungen für eine Route.
     *
     * @param routeId ID der Route
     * @return Liste der Bewertungen
     * @throws ResourceNotFoundException wenn die Route nicht existiert
     */
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatings(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route", routeId);
        }

        return ratingRepository.findByRouteId(routeId).stream()
                .map(RatingResponse::fromEntity)
                .toList();
    }
}
