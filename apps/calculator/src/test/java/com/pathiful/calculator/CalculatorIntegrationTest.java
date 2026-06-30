package com.pathiful.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CalculatorIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate rest;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        baseUrl = "http://localhost:" + port + "/api/calculator";
    }

    @Test
    void shouldAdd() {
        double result = calculate("2+3");
        assertEquals(5.0, result, 0.0001);
    }

    @Test
    void shouldMultiplyBeforeAdd() {
        double result = calculate("2+3*4");
        assertEquals(14.0, result, 0.0001);
    }

    @Test
    void shouldHandleParentheses() {
        double result = calculate("(2+3)*4");
        assertEquals(20.0, result, 0.0001);
    }

    @Test
    void shouldHandleNegativeNumbers() {
        double result = calculate("-3+2");
        assertEquals(-1.0, result, 0.0001);
    }

    @Test
    void shouldHandleDecimals() {
        double result = calculate("3.5+4");
        assertEquals(7.5, result, 0.0001);
    }

    @Test
    void shouldHandleWhitespace() {
        double result = calculate("  10  + 20 * 2  ");
        assertEquals(50.0, result, 0.0001);
    }

    @Test
    void shouldReturn400ForDivisionByZero() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl + "/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("expression", "1/0")),
                    String.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getResponseBodyAsString().contains("Division by zero"));
    }

    @Test
    void shouldReturn400ForInvalidExpression() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl + "/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("expression", "abc")),
                    String.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldReturn400ForEmptyExpression() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl + "/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("expression", "")),
                    String.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    private double calculate(String expression) {
        ResponseEntity<Map> resp = rest.exchange(
                RequestEntity
                        .post(baseUrl + "/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("expression", expression)),
                Map.class
        );
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Object result = resp.getBody().get("result");
        if (result instanceof Number n) {
            return n.doubleValue();
        }
        fail("result not a number: " + result);
        return 0;
    }
}
