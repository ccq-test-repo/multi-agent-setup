package com.pathiful.calculator;

import com.pathiful.calculator.controller.CalculatorController;
import com.pathiful.calculator.service.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer test for CalculatorController using the real CalculatorService.
 */
@WebMvcTest(CalculatorController.class)
@Import(CalculatorControllerTest.TestConfig.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        CalculatorService calculatorService() {
            return new CalculatorService();
        }
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn200WithResult() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"2+3*4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(14.0));
    }

    @Test
    void shouldHandleParentheses() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"(2+3)*4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(20.0));
    }

    @Test
    void shouldHandleNegativeNumbers() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"-3+2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-1.0));
    }

    @Test
    void shouldHandleDecimals() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"3.5+4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(7.5));
    }

    @Test
    void shouldHandleWhitespace() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"  10  + 20 * 2  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(50.0));
    }

    // -----------------------------------------------------------------------
    // Error cases — controller layer
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn400OnDivisionByZero() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1/0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Division by zero"));
    }

    @Test
    void shouldReturn400OnInvalidExpression() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnEmptyExpression() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnMissingExpressionField() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnNonJsonBody() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnNullExpression() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnMissingRequestBody() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Response format verification
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnResultAsNumber() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"42\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isNumber());
    }

    @Test
    void shouldReturnErrorAsString() throws Exception {
        mockMvc.perform(post("/api/calculator/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1/0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
    }
}
