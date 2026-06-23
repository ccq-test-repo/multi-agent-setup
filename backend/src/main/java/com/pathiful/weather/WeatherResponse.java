package com.pathiful.weather;

import java.time.LocalDateTime;

/**
 * Response-DTO for GET /api/weather.
 */
public class WeatherResponse {

    private Double temperature;
    private Double precipitation;
    private Double windSpeed;
    private Integer weatherCode;
    private LocalDateTime fetchedAt;
    private boolean cached;

    public WeatherResponse() {}

    public WeatherResponse(Double temperature, Double precipitation,
                           Double windSpeed, Integer weatherCode,
                           LocalDateTime fetchedAt, boolean cached) {
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
        this.fetchedAt = fetchedAt;
        this.cached = cached;
    }

    public static WeatherResponse fromCache(WeatherCache cache) {
        return new WeatherResponse(
                cache.getTemperature(),
                cache.getPrecipitation(),
                cache.getWindSpeed(),
                cache.getWeatherCode(),
                cache.getFetchedAt(),
                true
        );
    }

    public static WeatherResponse fresh(Double temperature, Double precipitation,
                                          Double windSpeed, Integer weatherCode) {
        return new WeatherResponse(
                temperature,
                precipitation,
                windSpeed,
                weatherCode,
                LocalDateTime.now(),
                false
        );
    }

    public Double getTemperature() { return temperature; }
    public Double getPrecipitation() { return precipitation; }
    public Double getWindSpeed() { return windSpeed; }
    public Integer getWeatherCode() { return weatherCode; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public boolean isCached() { return cached; }
}
