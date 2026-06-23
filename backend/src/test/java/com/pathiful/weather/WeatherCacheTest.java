package com.pathiful.weather;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class WeatherCacheTest {

    @Test
    void shouldSetAndGetProperties() {
        WeatherCache cache = new WeatherCache();
        cache.setLatitude(50.1109);
        cache.setLongitude(8.6821);
        cache.setTemperature(12.5);
        cache.setPrecipitation(0.0);
        cache.setWindSpeed(15.3);
        cache.setWeatherCode(500);
        cache.setFetchedAt(LocalDateTime.now().minusMinutes(5));
        cache.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertEquals(50.1109, cache.getLatitude(), 0.0001);
        assertEquals(8.6821, cache.getLongitude(), 0.0001);
        assertEquals(12.5, cache.getTemperature(), 0.001);
        assertEquals(0.0, cache.getPrecipitation(), 0.001);
        assertEquals(15.3, cache.getWindSpeed(), 0.001);
        assertEquals(500, cache.getWeatherCode());
        assertNotNull(cache.getFetchedAt());
        assertNotNull(cache.getExpiresAt());
    }

    @Test
    void shouldAllowNullWeatherCode() {
        WeatherCache cache = new WeatherCache();
        assertNull(cache.getWeatherCode());
    }

    @Test
    void shouldDefaultAllPrimitiveFieldsToZeroOrNull() {
        WeatherCache cache = new WeatherCache();
        assertNull(cache.getId());
        assertNull(cache.getLatitude());
        assertNull(cache.getLongitude());
        assertNull(cache.getTemperature());
        assertNull(cache.getPrecipitation());
        assertNull(cache.getWindSpeed());
        assertNull(cache.getWeatherCode());
        assertNull(cache.getFetchedAt());
        assertNull(cache.getExpiresAt());
    }
}
