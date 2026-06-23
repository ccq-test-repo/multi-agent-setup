package com.pathiful.weather;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather?lat={lat}&lon={lon}
     *
     * Liefert aktuelle Wetterdaten für die angegebenen Koordinaten.
     * Nutzt intern einen Cache mit 30 Minuten Gültigkeit.
     *
     * @param lat Breitengrad (-90 bis 90)
     * @param lon Längengrad (-180 bis 180)
     * @return WeatherResponse mit Temperatur, Niederschlag, Windgeschwindigkeit und Wettercode
     */
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam double lat,
            @RequestParam double lon,
            HttpServletRequest request) {

        WeatherResponse response = weatherService.getWeather(lat, lon);
        return ResponseEntity.ok(response);
    }
}
