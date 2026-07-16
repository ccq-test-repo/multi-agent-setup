package com.pathiful.calculator;

import com.pathiful.auth.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer test for DivideController (POST /api/calc/divide).
 * Tests the acceptance criteria derived from the issue spec, not from the
 * implementation internals.
 */
@WebMvcTest(DivideController.class)
@AutoConfigureMockMvc(addFilters = false)
class DivideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn200WithResult() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(5.0));
    }

    @Test
    void shouldHandleNonIntegerDivision() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":7,\"b\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(3.5));
    }

    @Test
    void shouldHandleNegativeDividend() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":-10,\"b\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-5.0));
    }

    @Test
    void shouldHandleNegativeDivisor() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":-2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-5.0));
    }

    @Test
    void shouldHandleDecimalInput() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":3.5,\"b\":0.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(7.0));
    }

    @Test
    void shouldHandleZeroDividend() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":0,\"b\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0.0));
    }

    // -----------------------------------------------------------------------
    // Division by zero
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn400OnDivisionByZero() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("division_by_zero"));
    }

    @Test
    void shouldReturn400OnDivisionByNegativeZero() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":-0.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("division_by_zero"));
    }

    // -----------------------------------------------------------------------
    // Invalid / missing input
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn400WhenAIsNull() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"b\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenBIsNull() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400WhenBothAreNull() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    /**
     * NOTE: Empty body causes HttpMessageNotReadableException before the controller
     * is reached. The codebase returns 500 because there is no handler for
     * HttpMessageNotReadableException in GlobalExceptionHandler.
     * Per AC this SHOULD return 400 -> requires infrastructure fix.
     */
    @Test
    void shouldReturn400OnEmptyBody() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    /**
     * NOTE: Non-JSON body causes HttpMessageNotReadableException before
     * controller is reached. Currently returns 500 (no handler).
     * Per AC this SHOULD return 400 -> requires infrastructure fix.
     */
    @Test
    void shouldReturn400OnNonJsonBody() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }

    /**
     * NOTE: NaN is not valid JSON; Jackson throws InvalidFormatException,
     * which falls through to GlobalExceptionHandler -> 500.
     * Per AC this SHOULD return 400 -> requires infrastructure fix.
     */
    @Test
    void shouldReturn400WhenAIsNaN() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":NaN,\"b\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBIsNaN() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":NaN}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAIsInfinity() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":Infinity,\"b\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBIsInfinity() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":10,\"b\":Infinity}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnStringInsteadOfNumber() throws Exception {
        mockMvc.perform(post("/api/calc/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":\"ten\",\"b\":2}"))
                .andExpect(status().isBadRequest());
    }
}
