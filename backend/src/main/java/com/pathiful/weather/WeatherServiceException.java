package com.pathiful.weather;

/**
 * Wird geworfen, wenn die Open-Meteo API nicht erreichbar ist
 * oder ein Fehler beim Abruf der Wetterdaten auftritt.
 */
public class WeatherServiceException extends RuntimeException {

    public WeatherServiceException(String message) {
        super(message);
    }

    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
