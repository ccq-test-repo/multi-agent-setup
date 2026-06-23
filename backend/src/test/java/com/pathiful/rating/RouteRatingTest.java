package com.pathiful.rating;

import static org.junit.jupiter.api.Assertions.*;

import com.pathiful.route.Route;
import com.pathiful.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class RouteRatingTest {

    @Test
    void shouldSetAndGetProperties() {
        Route route = new Route();
        User user = new User("rater@example.com", "hash", User.Role.USER);
        RouteRating rating = new RouteRating();

        rating.setRoute(route);
        rating.setUser(user);
        rating.setStars(4);
        rating.setComment("Schöne Strecke!");

        assertNull(rating.getId());
        assertSame(route, rating.getRoute());
        assertSame(user, rating.getUser());
        assertEquals(4, rating.getStars());
        assertEquals("Schöne Strecke!", rating.getComment());
    }

    @Test
    void shouldAllowNullComment() {
        RouteRating rating = new RouteRating();
        rating.setStars(1);
        assertNull(rating.getComment());
    }

    @Test
    void shouldAllowEmptyComment() {
        RouteRating rating = new RouteRating();
        rating.setStars(3);
        rating.setComment("");
        assertEquals("", rating.getComment());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        RouteRating rating = new RouteRating();
        assertNull(rating.getCreatedAt());

        rating.onCreate();

        assertNotNull(rating.getCreatedAt());
        assertTrue(rating.getCreatedAt() instanceof LocalDateTime);
    }

    @Test
    void shouldSetAndGetAllStarsValues() {
        for (int stars = 1; stars <= 5; stars++) {
            RouteRating rating = new RouteRating();
            rating.setStars(stars);
            assertEquals(stars, rating.getStars());
        }
    }

    @Test
    void shouldAllowLongComment() {
        RouteRating rating = new RouteRating();
        String longComment = "A".repeat(1000);
        rating.setStars(5);
        rating.setComment(longComment);
        assertEquals(1000, rating.getComment().length());
    }

    @Test
    void shouldReturnCorrectCreatedAtAfterSetting() {
        RouteRating rating = new RouteRating();
        LocalDateTime now = LocalDateTime.of(2026, 6, 23, 12, 0);
        rating.onCreate();
        // onCreate setzt auf now, das zum Zeitpunkt des Aufrufs erzeugt wird
        assertNotNull(rating.getCreatedAt());
    }

    @Test
    void shouldAllowMultipleRatingsOnDifferentRoutes() {
        Route route1 = new Route();
        Route route2 = new Route();
        User user = new User("user@example.com", "hash", User.Role.USER);

        RouteRating rating1 = new RouteRating();
        rating1.setRoute(route1);
        rating1.setUser(user);
        rating1.setStars(5);

        RouteRating rating2 = new RouteRating();
        rating2.setRoute(route2);
        rating2.setUser(user);
        rating2.setStars(3);

        assertSame(route1, rating1.getRoute());
        assertSame(route2, rating2.getRoute());
        assertSame(user, rating1.getUser());
        assertSame(user, rating2.getUser());
        assertEquals(5, rating1.getStars());
        assertEquals(3, rating2.getStars());
    }
}
