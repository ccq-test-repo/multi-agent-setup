package com.pathiful.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private RestTemplate restTemplate;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(weatherRepository, restTemplate);
        ReflectionTestUtils.setField(weatherService, "cacheMinutes", 30);
        ReflectionTestUtils.setField(weatherService, "openMeteoBaseUrl", "https://api.open-meteo.com/v1");
    }

    // -----------------------------------------------------------------------
    // Cache-Hit
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnCachedDataWhenValidCacheExists() {
        double lat = 52.52;
        double lon = 13.405;

        WeatherCache cache = new WeatherCache();
        cache.setLatitude(lat);
        cache.setLongitude(lon);
        cache.setTemperature(15.5);
        cache.setPrecipitation(0.0);
        cache.setWindSpeed(10.2);
        cache.setWeatherCode(1);
        cache.setFetchedAt(LocalDateTime.now().minusMinutes(5));
        cache.setExpiresAt(LocalDateTime.now().plusMinutes(25));

        when(weatherRepository.findTopByLatitudeAndLongitudeAndExpiresAtAfter(
                eq(lat), eq(lon), any(LocalDateTime.class)))
                .thenReturn(Optional.of(cache));

        WeatherResponse response = weatherService.getWeather(lat, lon);

        assertThat(response.getTemperature()).isEqualTo(15.5);
        assertThat(response.getPrecipitation()).isEqualTo(0.0);
        assertThat(response.getWindSpeed()).isEqualTo(10.2);
        assertThat(response.getWeatherCode()).isEqualTo(1);
        assertThat(response.isCached()).isTrue();

        verify(restTemplate, never()).getForObject(anyString(), any());
        verify(weatherRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Cache-Miss → API-Abfrage
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void shouldFetchFromApiAndCacheWhenNoValidCacheExists() {
        double lat = 48.135;
        double lon = 11.582;

        when(weatherRepository.findTopByLatitudeAndLongitudeAndExpiresAtAfter(
                eq(lat), eq(lon), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        Map<String, Object> current = Map.of(
                "temperature_2m", 22.3,
                "precipitation", 0.5,
                "wind_speed_10m", 5.1,
                "weather_code", 2
        );
        Map<String, Object> apiResponse = Map.of("current", current);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(apiResponse);

        WeatherResponse response = weatherService.getWeather(lat, lon);

        assertThat(response.getTemperature()).isEqualTo(22.3);
        assertThat(response.getPrecipitation()).isEqualTo(0.5);
        assertThat(response.getWindSpeed()).isEqualTo(5.1);
        assertThat(response.getWeatherCode()).isEqualTo(2);
        assertThat(response.isCached()).isFalse();

        verify(weatherRepository).save(argThat(cache ->
                cache.getLatitude().equals(lat)
                        && cache.getLongitude().equals(lon)
                        && cache.getTemperature().equals(22.3)
                        && cache.getExpiresAt().isAfter(LocalDateTime.now())));
    }

    // -----------------------------------------------------------------------
    // API-Fehler → lesbare Fehlermeldung
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowWeatherServiceExceptionWhenApiIsUnreachable() {
        double lat = 50.0;
        double lon = 10.0;

        when(weatherRepository.findTopByLatitudeAndLongitudeAndExpiresAtAfter(
                eq(lat), eq(lon), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(WeatherServiceException.class)
                .hasMessage("Wetterdienst ist derzeit nicht erreichbar. Bitte versuchen Sie es später erneut.");
    }

    @Test
    void shouldThrowWeatherServiceExceptionWhenApiResponseIsNull() {
        double lat = 50.0;
        double lon = 10.0;

        when(weatherRepository.findTopByLatitudeAndLongitudeAndExpiresAtAfter(
                eq(lat), eq(lon), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(WeatherServiceException.class)
                .hasMessage("Open-Meteo API lieferte keine Daten");
    }

    @Test
    void shouldThrowWeatherServiceExceptionWhenCurrentFieldIsMissing() {
        double lat = 50.0;
        double lon = 10.0;

        when(weatherRepository.findTopByLatitudeAndLongitudeAndExpiresAtAfter(
                eq(lat), eq(lon), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of());

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(WeatherServiceException.class)
                .hasMessage("Open-Meteo API: Feld 'current' fehlt in der Antwort");
    }

    // -----------------------------------------------------------------------
    // Ungültige Koordinaten
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidLatitude() {
        assertThatThrownBy(() -> weatherService.getWeather(100, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Breitengrad");

        assertThatThrownBy(() -> weatherService.getWeather(-91, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Breitengrad");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidLongitude() {
        assertThatThrownBy(() -> weatherService.getWeather(50, 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Längengrad");

        assertThatThrownBy(() -> weatherService.getWeather(50, -181))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Längengrad");
    }

    // -----------------------------------------------------------------------
    // Cache-TTL: Abgelaufener Cache → API-Abfrage
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void shouldFetchFromApiWhenCacheEntryIsExpired() {
        double lat = 51.0;
        double lon = 7.0;

        when(weatherRepository.findTopByLatitudeAndLongitudeAndExpiresAtAfter(
                eq(lat), eq(lon), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        Map<String, Object> current = Map.of(
                "temperature_2m", 18.0,
                "precipitation", 0.0,
                "wind_speed_10m", 3.3,
                "weather_code", 0
        );
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("current", current));

        WeatherResponse response = weatherService.getWeather(lat, lon);

        assertThat(response.isCached()).isFalse();
        assertThat(response.getTemperature()).isEqualTo(18.0);

        verify(weatherRepository).save(any());
    }
}
