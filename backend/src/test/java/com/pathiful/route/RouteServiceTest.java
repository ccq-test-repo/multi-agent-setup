package com.pathiful.route;

import com.pathiful.route.Route.RouteTransportMode;
import com.pathiful.route.Route.RouteType;
import com.pathiful.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RoutePointRepository routePointRepository;

    private final LandscapeScoringService scoringService = new LandscapeScoringService();

    @Mock
    private OrsRoutingService orsRoutingService;

    private RouteService routeService;

    private User owner;

    @Captor
    private ArgumentCaptor<Route> routeCaptor;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(routeRepository, routePointRepository, scoringService, orsRoutingService);
        ReflectionTestUtils.setField(routeService, "orsApiKey", "");
        owner = new User("user@example.com", "hash", User.Role.USER);
    }

    // -----------------------------------------------------------------------
    // Roundtrip – Erfolgsfall
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateRoundtripSuccessfully() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode("BIKE");
        request.setDistanceKm(10.0);
        request.setName("Abendrunde");

        Route savedRoute = new Route();
        savedRoute.setId(1L);
        savedRoute.setOwner(owner);
        savedRoute.setName("Abendrunde");
        savedRoute.setRouteType(RouteType.ROUNDTRIP);
        savedRoute.setTransportMode(RouteTransportMode.BIKE);
        savedRoute.setDistanceKm(10.0);
        savedRoute.setScenicScore(8);
        savedRoute.setVisibility(Route.Visibility.PRIVATE);

        when(routeRepository.save(any())).thenReturn(savedRoute);

        RouteResponse response = routeService.createRoundtrip(request, owner);

        assertThat(response).isNotNull();
        assertThat(response.getRouteType()).isEqualTo("ROUNDTRIP");
        assertThat(response.getTransportMode()).isEqualTo("BIKE");
        assertThat(response.getDistanceKm()).isEqualTo(10.0);
        assertThat(response.getScenicScore()).isNotNull();

        verify(routeRepository).save(any());
        verify(routePointRepository).saveAll(any());
    }

    // -----------------------------------------------------------------------
    // Destination – Erfolgsfall
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateDestinationSuccessfully() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setDestLat("48.250");
        request.setDestLon("11.700");
        request.setTransportMode("WALK");

        Route savedRoute = new Route();
        savedRoute.setId(2L);
        savedRoute.setOwner(owner);
        savedRoute.setName("Zielroute " + java.time.LocalDate.now());
        savedRoute.setRouteType(RouteType.DESTINATION);
        savedRoute.setTransportMode(RouteTransportMode.WALK);
        savedRoute.setDistanceKm(11.5);
        savedRoute.setScenicScore(8);
        savedRoute.setVisibility(Route.Visibility.PUBLIC);
        savedRoute.onCreate();

        when(routeRepository.save(any())).thenReturn(savedRoute);

        RouteResponse response = routeService.createDestination(request, owner);

        assertThat(response).isNotNull();
        assertThat(response.getRouteType()).isEqualTo("DESTINATION");
        assertThat(response.getTransportMode()).isEqualTo("WALK");
        assertThat(response.getDistanceKm()).isGreaterThan(0);

        verify(routeRepository).save(any());
    }

    // -----------------------------------------------------------------------
    // Validierung: Negative/fehlende Distanz bei Roundtrip
    // -----------------------------------------------------------------------

    @Test
    void shouldRejectRoundtripWithNullDistance() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode("BIKE");
        request.setDistanceKm(null);

        assertThatThrownBy(() -> routeService.createRoundtrip(request, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive Distanz");
    }

    @Test
    void shouldRejectRoundtripWithNegativeDistance() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode("BIKE");
        request.setDistanceKm(-5.0);

        assertThatThrownBy(() -> routeService.createRoundtrip(request, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive Distanz");
    }

    @Test
    void shouldRejectRoundtripWithZeroDistance() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode("WALK");
        request.setDistanceKm(0.0);

        assertThatThrownBy(() -> routeService.createRoundtrip(request, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive Distanz");
    }

    // -----------------------------------------------------------------------
    // Validierung: Ungültige Koordinaten (im Request, nicht im Service)
    // -----------------------------------------------------------------------

    @Test
    void shouldRejectInvalidTransportMode() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode("FLUGZEUG");
        request.setDistanceKm(10.0);

        assertThatThrownBy(() -> routeService.createRoundtrip(request, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nicht unterstütztes Verkehrsmittel");
    }

    @Test
    void shouldRejectNullTransportMode() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode(null);
        request.setDistanceKm(10.0);

        assertThatThrownBy(() -> routeService.createRoundtrip(request, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Verkehrsmittel");
    }

    // -----------------------------------------------------------------------
    // Destination ohne Zielkoordinaten
    // -----------------------------------------------------------------------

    @Test
    void shouldRejectDestinationWithoutDestCoordinates() {
        RouteRequest request = new RouteRequest();
        request.setStartLat("48.135");
        request.setStartLon("11.582");
        request.setTransportMode("CAR");

        assertThatThrownBy(() -> routeService.createDestination(request, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start- und Zielkoordinaten");
    }

    // -----------------------------------------------------------------------
    // Get / Delete
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnRouteForValidId() {
        Route route = new Route();
        route.setId(1L);
        route.setOwner(owner);
        route.setName("Test");
        route.setRouteType(RouteType.ROUNDTRIP);
        route.setTransportMode(RouteTransportMode.WALK);
        route.setVisibility(Route.Visibility.PRIVATE);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routePointRepository.findByRouteIdOrderBySequenceNumber(1L)).thenReturn(List.of());

        RouteResponse response = routeService.getRoute(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test");
    }

    @Test
    void shouldThrowForNonExistentRoute() {
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.getRoute(999L))
                .isInstanceOf(com.pathiful.common.ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldDeleteRouteSuccessfully() {
        when(routeRepository.existsById(1L)).thenReturn(true);

        routeService.deleteRoute(1L);

        verify(routePointRepository).deleteByRouteId(1L);
        verify(routeRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentRoute() {
        when(routeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> routeService.deleteRoute(999L))
                .isInstanceOf(com.pathiful.common.ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
