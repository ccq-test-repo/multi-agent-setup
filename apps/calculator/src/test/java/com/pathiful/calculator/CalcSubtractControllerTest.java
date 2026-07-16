package com.pathiful.calculator;

import com.pathiful.calculator.controller.CalcSubtractController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for CalcSubtractController.
 *
 * Acceptance Criteria (Issue #40):
 * - POST /api/calc/subtract with JSON {"a": number, "b": number}
 * - Response 200: {"result": number} → a - b
 * - Response 400: {"error":"invalid_input"} on invalid input
 */
@WebMvcTest(CalcSubtractController.class)
class CalcSubtractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void shouldSubtractTwoPositiveNumbers() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":5,\"b\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2.0));
    }

    @Test
    void shouldSubtractResultingInNegative() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":3,\"b\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-5.0));
    }

    @Test
    void shouldSubtractWithDecimalNumbers() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10.5,\"b\":3.2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(7.3));
    }

    @Test
    void shouldSubtractWithNegativeA() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":-5,\"b\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-8.0));
    }

    @Test
    void shouldSubtractWithNegativeB() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":5,\"b\":-3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(8.0));
    }

    @Test
    void shouldSubtractZero() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":7,\"b\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(7.0));
    }

    @Test
    void shouldHandleIdenticalNumbers() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":42,\"b\":42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0.0));
    }

    // -----------------------------------------------------------------------
    // Error cases — missing / invalid fields
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn400WhenAIsNull() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":null,\"b\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenBIsNull() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":5,\"b\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenBothAreNull() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":null,\"b\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnMissingAField() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"b\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnMissingBField() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnEmptyBody() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnNonJsonBody() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnMissingRequestBody() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAIsString() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":\"abc\",\"b\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBIsString() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":5,\"b\":\"xyz\"}"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Response format verification
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnResultAsNumber() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isNumber());
    }

    @Test
    void shouldReturnErrorAsString() throws Exception {
        mockMvc.perform(post("/api/calc/subtract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":null,\"b\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
    }
}
