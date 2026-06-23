package com.pathiful.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service for fetching weather data from Open-Meteo with caching.
 *
 * Cache validity period: 30 minutes (configurable via weather.cache.minutes).
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate;

    @Value("${open-meteo.base-url:https://api.open-meteo.com/v1}")
    private String openMeteoBaseUrl;

    @Value("${weather.cache.minutes:30}")
    private int cacheMinutes;

    public WeatherService(WeatherRepository weatherRepository, RestTemplate restTemplate) {
        this.weatherRepository = weatherRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Liefert die aktuellen Wetterdaten für die angegebenen Koordinaten.
     * Nutzt den Cache, falls ein gültiger Eintrag existiert (expires_at > now),
     * sonst wird die Open-Meteo API abgefragt.
     *
     * @param lat  Breitengrad
     * @param lon  Längengrad
     * @return WeatherResponse mit Wetterdaten und Cache-Status
     * @throws WeatherServiceException bei API-Fehlern oder ungültigen Koordinaten
     */
    @Transactional
    public WeatherResponse getWeather(double lat, double lon) {
        validateCoordinates(lat, lon);

        // 1. Cache-Hit prüfen
        Optional<WeatherCache> cached = weatherRepository
                .findTopByLatitudeAndLongitudeAndExpiresAtAfter(lat, lon, LocalDateTime.now());

        if (cached.isPresent()) {
            log.debug("Weather cache hit for lat={}, lon={}", lat, lon);
            return WeatherResponse.fromCache(cached.get());
        }

        // 2. Open-Meteo API abfragen
        log.debug("Weather cache miss for lat={}, lon={} – fetching from Open-Meteo", lat, lon);
        WeatherResponse response = fetchFromOpenMeteo(lat, lon);

        // 3. Im Cache speichern
        saveToCache(lat, lon, response);

        return response;
    }

    private void validateCoordinates(double lat, double lon) {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Ungültiger Breitengrad: " + lat + " (muss zwischen -90 und 90 liegen)");
        }
        if (lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Ungültiger Längengrad: " + lon + " (muss zwischen -180 und 180 liegen)");
        }
    }

    @SuppressWarnings("unchecked")
    private WeatherResponse fetchFromOpenMeteo(double lat, double lon) {
        String url = String.format(
                "%s/forecast?latitude=%s&longitude=%s&current=temperature_2m,precipitation,wind_speed_10m,weather_code&timezone=auto",
                openMeteoBaseUrl, lat, lon
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new WeatherServiceException("Open-Meteo API lieferte keine Daten");
            }

            Map<String, Object> current = (Map<String, Object>) response.get("current");
            if (current == null) {
                throw new WeatherServiceException("Open-Meteo API: Feld 'current' fehlt in der Antwort");
            }

            Double temperature = getNumberValue(current.get("temperature_2m"));
            Double precipitation = getNumberValue(current.get("precipitation"));
            Double windSpeed = getNumberValue(current.get("wind_speed_10m"));
            Integer weatherCode = getIntegerValue(current.get("weather_code"));

            if (temperature == null) {
                throw new WeatherServiceException("Open-Meteo API: Feld 'temperature_2m' fehlt oder ist ungültig");
            }

            return WeatherResponse.fresh(temperature, precipitation, windSpeed, weatherCode);

        } catch (RestClientException e) {
            log.error("Open-Meteo API nicht erreichbar für lat={}, lon={}: {}", lat, lon, e.getMessage());
            throw new WeatherServiceException("Wetterdienst ist derzeit nicht erreichbar. Bitte versuchen Sie es später erneut.");
        }
    }

    private void saveToCache(double lat, double lon, WeatherResponse response) {
        WeatherCache cache = new WeatherCache();
        cache.setLatitude(lat);
        cache.setLongitude(lon);
        cache.setTemperature(response.getTemperature());
        cache.setPrecipitation(response.getPrecipitation());
        cache.setWindSpeed(response.getWindSpeed());
        cache.setWeatherCode(response.getWeatherCode());
        cache.setFetchedAt(LocalDateTime.now());
        cache.setExpiresAt(LocalDateTime.now().plusMinutes(cacheMinutes));

        weatherRepository.save(cache);
        log.debug("Weather data cached for lat={}, lon={}, expires at {}", lat, lon, cache.getExpiresAt());
    }

    // -- Hilfsmethoden für response parsing

    private static Double getNumberValue(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }

    private static Integer getIntegerValue(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return null;
    }
}
