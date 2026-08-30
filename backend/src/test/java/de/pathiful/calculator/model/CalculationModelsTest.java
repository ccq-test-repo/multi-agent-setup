package de.pathiful.calculator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verhaltenstests fuer die Modelle {@link CalculationRequest} und
 * {@link CalculationResponse}, abgeleitet aus den Akzeptanzkriterien des
 * Issues #88: Konstruktion, Zugriff auf die Komponenten sowie
 * {@code equals}/{@code hashCode} der Records.
 */
class CalculationModelsTest {

    @Nested
    @DisplayName("CalculationRequest")
    class Request {

        @Test
        @DisplayName("Konstruktion speichert und liefert die Komponenten")
        void constructsAndAccesses() {
            CalculationRequest request =
                    new CalculationRequest(new BigDecimal("2"), "ADD", new BigDecimal("3"));

            assertEquals(new BigDecimal("2"), request.left());
            assertEquals("ADD", request.operator());
            assertEquals(new BigDecimal("3"), request.right());
        }

        @Test
        @DisplayName("equals: gleiche Komponenten sind gleich")
        void equalWhenSameComponents() {
            CalculationRequest a =
                    new CalculationRequest(new BigDecimal("2"), "ADD", new BigDecimal("3"));
            CalculationRequest b =
                    new CalculationRequest(new BigDecimal("2"), "ADD", new BigDecimal("3"));

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("equals: unterschiedlicher Operator ist nicht gleich")
        void notEqualWhenOperatorDiffers() {
            CalculationRequest a =
                    new CalculationRequest(new BigDecimal("2"), "ADD", new BigDecimal("3"));
            CalculationRequest b =
                    new CalculationRequest(new BigDecimal("2"), "SUBTRACT", new BigDecimal("3"));

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals: unterschiedlicher Operand ist nicht gleich")
        void notEqualWhenOperandDiffers() {
            CalculationRequest a =
                    new CalculationRequest(new BigDecimal("2"), "ADD", new BigDecimal("3"));
            CalculationRequest b =
                    new CalculationRequest(new BigDecimal("2"), "ADD", new BigDecimal("4"));

            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("CalculationResponse")
    class Response {

        @Test
        @DisplayName("Konstruktion speichert und liefert das Ergebnis")
        void constructsAndAccesses() {
            CalculationResponse response = new CalculationResponse(new BigDecimal("5"));

            assertEquals(new BigDecimal("5"), response.result());
        }

        @Test
        @DisplayName("equals: gleiches Ergebnis ist gleich")
        void equalWhenSameResult() {
            CalculationResponse a = new CalculationResponse(new BigDecimal("5"));
            CalculationResponse b = new CalculationResponse(new BigDecimal("5"));

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("equals: unterschiedliches Ergebnis ist nicht gleich")
        void notEqualWhenResultDiffers() {
            CalculationResponse a = new CalculationResponse(new BigDecimal("5"));
            CalculationResponse b = new CalculationResponse(new BigDecimal("7"));

            assertNotEquals(a, b);
        }
    }
}
