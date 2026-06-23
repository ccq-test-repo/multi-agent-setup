package com.pathiful.rating;

import static org.junit.jupiter.api.Assertions.*;

import com.pathiful.route.Route;
import com.pathiful.user.User;
import org.junit.jupiter.api.Test;

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
    void shouldSetCreatedAtOnPrePersist() {
        RouteRating rating = new RouteRating();
        assertNull(rating.getCreatedAt());

        rating.onCreate();

        assertNotNull(rating.getCreatedAt());
    }
}
