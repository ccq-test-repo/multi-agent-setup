package de.pathiful.calculator.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pathiful.calculator.model.CalculationRequest;
import de.pathiful.calculator.service.CalculatorService;
import de.pathiful.calculator.service.DivisionByZeroException;
import de.pathiful.calculator.service.UnknownOperatorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verhaltenstests fuer den {@link CalculatorController}, abgeleitet aus den
 * Akzeptanzkriterien des Issues #88.
 *
 * <p>Geprueft werden die vier Antwortwege von {@code POST /api/calculate}
 * (gültige Rechnung 200, Division durch null 400 / DIVISION_BY_ZERO,
 * unbekannter Operator 400 / UNKNOWN_OPERATOR, fehlende Felder 400 /
 * INVALID_REQUEST) sowie {@code GET /api/health}. Der Service wird gemockt —
 * nur das Controller-Verhalten wird getestet, nicht die Rechenlogik.</p>
 */
@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalculatorService service;

    @Nested
    @DisplayName("POST /api/calculate — gültige Rechnung")
    class ValidCalculation {

        @Test
        @DisplayName("gültige Anfrage liefert HTTP 200 mit dem Ergebnis")
        void returnsOkWithResult() throws Exception {
            when(service.calculate(eq(new BigDecimal("2")), eq("ADD"), eq(new BigDecimal("3"))))
                    .thenReturn(new BigDecimal("5"));

            mockMvc.perform(post("/api/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"left\":2,\"operator\":\"ADD\",\"right\":3}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(5));
        }
    }

    @Nested
    @DisplayName("POST /api/calculate — Fehlerfaelle")
    class ErrorCases {

        @Test
        @DisplayName("Division durch null liefert HTTP 400 mit DIVISION_BY_ZERO")
        void divisionByZeroReturns400() throws Exception {
            when(service.calculate(any(), any(), any()))
                    .thenThrow(new DivisionByZeroException());

            mockMvc.perform(post("/api/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"left\":1,\"operator\":\"DIVIDE\",\"right\":0}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("DIVISION_BY_ZERO"));
        }

        @Test
        @DisplayName("unbekannter Operator liefert HTTP 400 mit UNKNOWN_OPERATOR")
        void unknownOperatorReturns400() throws Exception {
            when(service.calculate(any(), any(), any()))
                    .thenThrow(new UnknownOperatorException("FOO"));

            mockMvc.perform(post("/api/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"left\":1,\"operator\":\"FOO\",\"right\":2}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("UNKNOWN_OPERATOR"));
        }

        @Test
        @DisplayName("fehlender Operand (null right) liefert HTTP 400 mit INVALID_REQUEST")
        void missingFieldReturns400() throws Exception {
            mockMvc.perform(post("/api/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"left\":1,\"operator\":\"ADD\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("leerer Operator (blank) liefert HTTP 400 mit INVALID_REQUEST")
        void blankOperatorReturns400() throws Exception {
            mockMvc.perform(post("/api/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"left\":1,\"operator\":\"   \",\"right\":2}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        }
    }

    @Nested
    @DisplayName("GET /api/health")
    class Health {

        @Test
        @DisplayName("liefert HTTP 200 mit status UP")
        void healthIsUp() throws Exception {
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }
}
