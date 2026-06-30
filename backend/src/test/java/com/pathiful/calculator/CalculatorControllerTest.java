package com.pathiful.calculator;

import com.pathiful.auth.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer test for CalculatorController.
 */
@WebMvcTest(CalculatorController.class)
@AutoConfigureMockMvc(addFilters = false)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculatorService calculatorService;

    @MockBean
    private TokenService tokenService;

    @Test
    void shouldReturn200WithResult() throws Exception {
        when(calculatorService.evaluate("2+3*4")).thenReturn(14.0);

        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"2+3*4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(14.0));
    }

    @Test
    void shouldReturn400OnDivisionByZero() throws Exception {
        when(calculatorService.evaluate(anyString()))
                .thenThrow(new IllegalArgumentException("Division by zero"));

        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"5/0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Division by zero"));
    }

    @Test
    void shouldReturn400OnInvalidExpression() throws Exception {
        when(calculatorService.evaluate(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid expression"));

        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"2+abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid expression"));
    }

    @Test
    void shouldReturn400OnEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnMissingExpression() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldComputeCorrectlyUsingRealService() throws Exception {
        when(calculatorService.evaluate("10+6/3")).thenReturn(12.0);

        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"10+6/3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(12.0));
    }
}
