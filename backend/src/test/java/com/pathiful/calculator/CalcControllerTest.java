package com.pathiful.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for CalcController – POST /api/calc/multiply.
 *
 * Derived from the acceptance criteria:
 * - "Gültige Zahlen werden akzeptiert"
 * - "ungültige/nicht-numerische Eingaben geben HTTP 400 mit {\"error\":\"invalid_input\"}"
 */
@WebMvcTest(CalcController.class)
class CalcControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Happy Path -------------------------------------------------------

    @Test
    void shouldMultiplyTwoPositiveIntegers() throws Exception {
        // a = 2, b = 3 → result = 6
        var request = new MultiplyRequest(2.0, 3.0);

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(6.0));
    }

    @Test
    void shouldMultiplyPositiveAndNegativeNumber() throws Exception {
        // a = -4.0, b = 5.0 → result = -20.0
        var request = new MultiplyRequest(-4.0, 5.0);

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-20.0));
    }

    @Test
    void shouldMultiplyByZero() throws Exception {
        // a = 0, b = 100 → result = 0
        var request = new MultiplyRequest(0.0, 100.0);

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0.0));
    }

    @Test
    void shouldMultiplyDecimalNumbers() throws Exception {
        // a = 1.5, b = 2.5 → result = 3.75
        var request = new MultiplyRequest(1.5, 2.5);

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(3.75));
    }

    // --- Error Cases: invalid / non-numeric inputs -----------------------

    @Test
    void shouldReturn400WhenANull() throws Exception {
        // a = null
        var body = objectMapper.writeValueAsString(Map.of("b", 5.0));

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenBNull() throws Exception {
        // b = null
        var body = objectMapper.writeValueAsString(Map.of("a", 3.0));

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenBothNull() throws Exception {
        // both fields missing
        var body = "{}";

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenAIsNaN() throws Exception {
        // a = NaN
        var body = "{\"a\": NaN, \"b\": 5.0}";
        // NaN is not valid JSON – use a workaround: send nonsense and expect 400
        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAIsInfinity() throws Exception {
        var body = "{\"a\": Infinity, \"b\": 5.0}";
        // Infinity not valid JSON either
        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForNonNumericStringInput() throws Exception {
        var body = "{\"a\": \"abc\", \"b\": 5.0}";

        mockMvc.perform(post("/api/calc/multiply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
