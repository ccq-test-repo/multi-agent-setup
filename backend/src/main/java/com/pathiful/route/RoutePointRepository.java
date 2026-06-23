package com.pathiful.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {

    List<RoutePoint> findByRouteIdOrderBySequenceNumber(Long routeId);

    void deleteByRouteId(Long routeId);
}
