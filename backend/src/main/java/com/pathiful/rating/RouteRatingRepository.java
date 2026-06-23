package com.pathiful.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRatingRepository extends JpaRepository<RouteRating, Long> {

    List<RouteRating> findByRouteId(Long routeId);

    Optional<RouteRating> findByRouteIdAndUserId(Long routeId, Long userId);
}
