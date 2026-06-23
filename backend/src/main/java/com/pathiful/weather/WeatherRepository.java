package com.pathiful.weather;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<WeatherCache, Long> {

    Optional<WeatherCache> findTopByLatitudeAndLongitudeAndExpiresAtAfter(
            Double latitude,
            Double longitude,
            LocalDateTime now
    );
}
