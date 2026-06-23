package com.pathiful;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic context-load test to verify the Spring Boot application starts.
 * Uses "test" profile so Flyway migrations and DB-dependent beans are
 * excluded or swapped for light context checks.
 */
@SpringBootTest
@ActiveProfiles("test")
class PathifulApplicationTests {

    @Test
    void contextLoads() {
        // ApplicationContext loads successfully
    }
}
