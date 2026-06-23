package com.pathiful.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Verifies that the CorsConfig bean is created and returns a WebMvcConfigurer.
 */
@SpringBootTest
@ActiveProfiles("test")
class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Test
    void shouldCreateWebMvcConfigurerBean() {
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        assertNotNull(configurer);
    }
}
