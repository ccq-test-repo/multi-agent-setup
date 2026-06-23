package com.pathiful.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Route> findAllByOrderByCreatedAtDesc();
}
