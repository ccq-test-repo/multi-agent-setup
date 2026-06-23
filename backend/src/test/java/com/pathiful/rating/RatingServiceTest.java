package com.pathiful.rating;

import com.pathiful.common.ResourceNotFoundException;
import com.pathiful.route.Route;
import com.pathiful.route.RoutePointRepository;
import com.pathiful.route.RouteRepository;
import com.pathiful.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RouteRatingRepository ratingRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RoutePointRepository routePointRepository;

    @Captor
    private ArgumentCaptor<RouteRating> ratingCaptor;

    private RatingService ratingService;

    private Route route;
    private User user;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService(ratingRepository, routeRepository, routePointRepository);

        route = new Route();
        route.setId(1L);
        route.setName("Test-Route");

        user = new User("tester@example.com", "hash", User.Role.USER);
        user.setId(42L);
    }

    // -----------------------------------------------------------------------
    // createRating – Success
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateRatingSuccessfully() {
        RatingRequest request = new RatingRequest();
        request.setStars(4);
        request.setComment("Schöne Strecke!");

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(ratingRepository.findByRouteIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        RouteRating savedRating = new RouteRating();
        savedRating.setId(10L);
        savedRating.setRoute(route);
        savedRating.setUser(user);
        savedRating.setStars(4);
        savedRating.setComment("Schöne Strecke!");
        savedRating.onCreate();

        when(ratingRepository.save(any())).thenReturn(savedRating);

        RatingResponse response = ratingService.createRating(1L, user, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRouteId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(42L);
        assertThat(response.getStars()).isEqualTo(4);
        assertThat(response.getComment()).isEqualTo("Schöne Strecke!");
        assertThat(response.getCreatedAt()).isNotNull();

        verify(ratingRepository).save(ratingCaptor.capture());
        RouteRating captured = ratingCaptor.getValue();
        assertThat(captured.getStars()).isEqualTo(4);
        assertThat(captured.getComment()).isEqualTo("Schöne Strecke!");
        assertThat(captured.getRoute()).isSameAs(route);
        assertThat(captured.getUser()).isSameAs(user);
    }

    // -----------------------------------------------------------------------
    // createRating – With null comment (optional)
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateRatingWithNullComment() {
        RatingRequest request = new RatingRequest();
        request.setStars(5);
        request.setComment(null);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(ratingRepository.findByRouteIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        RouteRating savedRating = new RouteRating();
        savedRating.setId(11L);
        savedRating.setRoute(route);
        savedRating.setUser(user);
        savedRating.setStars(5);
        savedRating.setComment(null);
        savedRating.onCreate();

        when(ratingRepository.save(any())).thenReturn(savedRating);

        RatingResponse response = ratingService.createRating(1L, user, request);

        assertThat(response.getStars()).isEqualTo(5);
        assertThat(response.getComment()).isNull();
    }

    // -----------------------------------------------------------------------
    // createRating – Duplicate rating
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowDuplicateRatingExceptionWhenAlreadyRated() {
        RatingRequest request = new RatingRequest();
        request.setStars(3);
        request.setComment("Ok");

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        RouteRating existing = new RouteRating();
        existing.setStars(3);
        when(ratingRepository.findByRouteIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> ratingService.createRating(1L, user, request))
                .isInstanceOf(DuplicateRatingException.class)
                .hasMessageContaining("bereits")
                .hasMessageContaining("1")
                .hasMessageContaining("42");

        verify(ratingRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // createRating – Route not found
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowResourceNotFoundExceptionWhenRouteDoesNotExist() {
        RatingRequest request = new RatingRequest();
        request.setStars(2);

        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.createRating(999L, user, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ratingRepository, never()).findByRouteIdAndUserId(any(), any());
        verify(ratingRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getRatings – Success (multiple ratings)
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnRatingsForRoute() {
        User otherUser = new User("other@example.com", "hash", User.Role.USER);
        otherUser.setId(99L);

        RouteRating r1 = new RouteRating();
        r1.setId(1L);
        r1.setRoute(route);
        r1.setUser(user);
        r1.setStars(5);
        r1.setComment("Tolle Route!");
        r1.onCreate();

        RouteRating r2 = new RouteRating();
        r2.setId(2L);
        r2.setRoute(route);
        r2.setUser(otherUser);
        r2.setStars(3);
        r2.setComment("Ganz ok");
        r2.onCreate();

        when(routeRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByRouteId(1L)).thenReturn(List.of(r1, r2));

        List<RatingResponse> ratings = ratingService.getRatings(1L);

        assertThat(ratings).hasSize(2);

        assertThat(ratings.get(0).getId()).isEqualTo(1L);
        assertThat(ratings.get(0).getStars()).isEqualTo(5);
        assertThat(ratings.get(0).getComment()).isEqualTo("Tolle Route!");
        assertThat(ratings.get(0).getUserId()).isEqualTo(42L);

        assertThat(ratings.get(1).getId()).isEqualTo(2L);
        assertThat(ratings.get(1).getStars()).isEqualTo(3);
        assertThat(ratings.get(1).getComment()).isEqualTo("Ganz ok");
        assertThat(ratings.get(1).getUserId()).isEqualTo(99L);
    }

    // -----------------------------------------------------------------------
    // getRatings – Empty list for unrated route
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnEmptyListForUnratedRoute() {
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByRouteId(1L)).thenReturn(List.of());

        List<RatingResponse> ratings = ratingService.getRatings(1L);

        assertThat(ratings).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getRatings – Route not found
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGettingRatingsForNonExistentRoute() {
        when(routeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> ratingService.getRatings(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ratingRepository, never()).findByRouteId(any());
    }

    // -----------------------------------------------------------------------
    // DuplicateRatingException – direct test
    // -----------------------------------------------------------------------

    @Test
    void duplicateRatingExceptionShouldHaveCorrectMessage() {
        DuplicateRatingException ex = new DuplicateRatingException(1L, 42L);
        assertThat(ex.getMessage()).contains("1").contains("42");
    }
}
