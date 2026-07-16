package com.pathiful.calculator;

import com.pathiful.calculator.controller.calc.CalcAddController;
import com.pathiful.calculator.service.calc.CalcAddService;
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
 * Web-layer test for CalcAddController using the real CalcAddService.
 *
 * Tests are derived from the acceptance criteria for POST /api/calc/add.
 */
@WebMvcTest(CalcAddController.class)
@Import(CalcAddControllerTest.TestConfig.class)
class CalcAddControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        CalcAddService calcAddService() {
            return new CalcAddService();
        }
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn200WithResult() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":3,\"b\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(8.0));
    }

    @Test
    void shouldHandlePositiveIntegers() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":0,\"b\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0.0));
    }

    @Test
    void shouldHandleNegativeNumbers() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":-10,\"b\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-7.0));
    }

    @Test
    void shouldHandleNegativeSum() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":-5,\"b\":-3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-8.0));
    }

    @Test
    void shouldHandleDecimalNumbers() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":2.5,\"b\":3.7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(6.2));
    }

    @Test
    void shouldHandleLargeNumbers() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":1000000,\"b\":2000000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(3000000.0));
    }

    // -----------------------------------------------------------------------
    // Error cases — non-numeric / invalid input → HTTP 400 + {"error":"invalid_input"}
    // -----------------------------------------------------------------------

    @Test
    void shouldReturn400OnNonNumericA() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":\"abc\",\"b\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnNonNumericB() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":5,\"b\":\"xyz\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnNonNumericBoth() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":\"abc\",\"b\":\"def\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnNullA() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":null,\"b\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnNullB() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":3,\"b\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnMissingAField() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"b\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnMissingBField() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnEmptyBody() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnNonJsonBody() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnMissingRequestBody() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturn400OnObjectInsteadOfNumber() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":{},\"b\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    // -----------------------------------------------------------------------
    // Response format verification
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnResultAsNumber() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":7,\"b\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isNumber());
    }

    @Test
    void shouldReturnErrorAsString() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":\"abc\",\"b\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.error").value("invalid_input"));
    }

    @Test
    void shouldReturnResultNotErrorOnSuccess() throws Exception {
        mockMvc.perform(post("/api/calc/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":3,\"b\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
