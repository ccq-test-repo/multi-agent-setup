package com.pathiful.route;

import com.pathiful.auth.TokenService;
import com.pathiful.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests für den RouteController an der HTTP-Schnittstelle.
 * Prüft JSON-Serialisierung, HTTP-Statuscodes und Fehlerantworten.
 */
@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteService routeService;

    @MockBean
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "hash", User.Role.USER);
        testUser.setId(1L);
    }

    /** Post-processor that sets the given User as the @AuthenticationPrincipal. */
    private RequestPostProcessor withUser(User user) {
        return request -> {
            var auth = UsernamePasswordAuthenticationToken.authenticated(
                    user, null, List.of(() -> "ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(auth);
            return request;
        };
    }

    // -----------------------------------------------------------------------
    // POST /api/routes/roundtrip – Erfolgsfall
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateRoundtrip() throws Exception {
        RouteResponse response = new RouteResponse();
        response.setId(1L);
        response.setName("Testrunde");
        response.setRouteType("ROUNDTRIP");
        response.setTransportMode("BIKE");
        response.setDistanceKm(10.0);
        response.setScenicScore(7);

        when(routeService.createRoundtrip(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "11.582",
                                    "transportMode": "BIKE",
                                    "distanceKm": 10.0,
                                    "name": "Testrunde"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("ROUNDTRIP"))
                .andExpect(jsonPath("$.transportMode").value("BIKE"))
                .andExpect(jsonPath("$.totalDistance").value(10.0))
                .andExpect(jsonPath("$.sceneryScore").value(7));
    }

    // -----------------------------------------------------------------------
    // POST /api/routes/roundtrip – Validierung
    // -----------------------------------------------------------------------

    @Test
    void shouldRejectRoundtripWithMissingStartLat() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLon": "11.582",
                                    "transportMode": "BIKE",
                                    "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRoundtripWithMissingStartLon() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "transportMode": "BIKE",
                                    "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRoundtripWithInvalidLatitude() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "99.999",
                                    "startLon": "11.582",
                                    "transportMode": "BIKE",
                                    "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRoundtripWithInvalidLongitude() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "199.999",
                                    "transportMode": "BIKE",
                                    "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRoundtripWithEmptyTransportMode() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "11.582",
                                    "transportMode": "",
                                    "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRoundtripWithNullTransportMode() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "11.582",
                                    "transportMode": null,
                                    "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRoundtripWithNegativeDistance() throws Exception {
        mockMvc.perform(post("/api/routes/roundtrip")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "11.582",
                                    "transportMode": "BIKE",
                                    "distanceKm": -5.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // POST /api/routes/destination – Erfolgsfall
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateDestination() throws Exception {
        RouteResponse response = new RouteResponse();
        response.setId(2L);
        response.setName("Testziel");
        response.setRouteType("DESTINATION");
        response.setTransportMode("WALK");
        response.setDistanceKm(12.5);
        response.setScenicScore(8);

        when(routeService.createDestination(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/routes/destination")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "11.582",
                                    "destLat": "48.250",
                                    "destLon": "11.700",
                                    "transportMode": "WALK"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.type").value("DESTINATION"))
                .andExpect(jsonPath("$.transportMode").value("WALK"))
                .andExpect(jsonPath("$.totalDistance").value(12.5));
    }

    @Test
    void shouldCreateDestinationWithCar() throws Exception {
        RouteResponse response = new RouteResponse();
        response.setId(3L);
        response.setName("Autofahrt");
        response.setRouteType("DESTINATION");
        response.setTransportMode("CAR");
        response.setDistanceKm(50.0);

        when(routeService.createDestination(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/routes/destination")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLat": "48.135",
                                    "startLon": "11.582",
                                    "destLat": "48.500",
                                    "destLon": "12.000",
                                    "transportMode": "CAR",
                                    "name": "Autofahrt"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transportMode").value("CAR"));
    }

    @Test
    void shouldRejectDestinationWithMissingStartCoord() throws Exception {
        mockMvc.perform(post("/api/routes/destination")
                        .with(withUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startLon": "11.582",
                                    "destLat": "48.250",
                                    "destLon": "11.700",
                                    "transportMode": "WALK"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // GET /api/routes/{id}
    // -----------------------------------------------------------------------

    @Test
    void shouldGetRoute() throws Exception {
        RouteResponse response = new RouteResponse();
        response.setId(1L);
        response.setName("Gespeicherte Route");
        response.setRouteType("ROUNDTRIP");
        response.setTransportMode("BIKE");
        response.setScenicScore(7);

        RouteResponse.RoutePointDto pointDto = new RouteResponse.RoutePointDto();
        pointDto.setSequenceNumber(0);
        pointDto.setPointType("START");
        pointDto.setLat(48.135);
        pointDto.setLon(11.582);
        response.setPoints(List.of(pointDto));

        when(routeService.getRoute(eq(1L), any(User.class))).thenReturn(response);

        mockMvc.perform(get("/api/routes/1").with(withUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gespeicherte Route"))
                .andExpect(jsonPath("$.points[0].latitude").value(48.135))
                .andExpect(jsonPath("$.points[0].longitude").value(11.582));
    }

    @Test
    void shouldReturn404ForNonExistentRoute() throws Exception {
        when(routeService.getRoute(eq(999L), any(User.class)))
                .thenThrow(new com.pathiful.common.ResourceNotFoundException("Route", 999L));

        mockMvc.perform(get("/api/routes/999").with(withUser(testUser)))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // DELETE /api/routes/{id}
    // -----------------------------------------------------------------------

    @Test
    void shouldDeleteRoute() throws Exception {
        mockMvc.perform(delete("/api/routes/1").with(withUser(testUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404ForDeletingNonExistentRoute() throws Exception {
        doThrow(new com.pathiful.common.ResourceNotFoundException("Route", 999L))
                .when(routeService).deleteRoute(eq(999L), any(User.class));

        mockMvc.perform(delete("/api/routes/999").with(withUser(testUser)))
                .andExpect(status().isNotFound());
    }
}
