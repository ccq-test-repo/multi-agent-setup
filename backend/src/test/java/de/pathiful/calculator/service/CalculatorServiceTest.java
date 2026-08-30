package de.pathiful.calculator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit-Tests fuer den {@link CalculatorService}.
 *
 * <p>Die erwarteten Ergebnisse sind aus den Akzeptanzkriterien des Issues #82
 * abgeleitet, nicht aus der Implementierung: 10 Nachkommastellen kaufmaennisch
 * gerundet, ueberfluessige Nullen entfallen, Division durch null und unbekannter
 * Operator werden abgelehnt, gerechnet wird mit {@link BigDecimal} (kein double).</p>
 */
class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Nested
    @DisplayName("ADD")
    class Add {
        @Test
        @DisplayName("0.1 + 0.2 ergibt exakt 0.3 (BigDecimal, kein double-Artefakt)")
        void addDoesNotSufferFromFloatingPoint() {
            BigDecimal result = service.calculate(new BigDecimal("0.1"), "ADD", new BigDecimal("0.2"));
            assertEquals(new BigDecimal("0.3"), result);
        }

        @Test
        @DisplayName("12.5 + 4 ergibt 16.5")
        void addTwoDecimals() {
            BigDecimal result = service.calculate(new BigDecimal("12.5"), "ADD", new BigDecimal("4"));
            assertEquals(new BigDecimal("16.5"), result);
        }

        @Test
        @DisplayName("negative Zahlen: -7 + (-3) ergibt -10")
        void addNegatives() {
            BigDecimal result = service.calculate(new BigDecimal("-7"), "ADD", new BigDecimal("-3"));
            assertEquals(new BigDecimal("-10"), result);
        }
    }

    @Nested
    @DisplayName("SUBTRACT")
    class Subtract {
        @Test
        @DisplayName("12.5 - 4 ergibt 8.5")
        void subtractBasic() {
            BigDecimal result = service.calculate(new BigDecimal("12.5"), "SUBTRACT", new BigDecimal("4"));
            assertEquals(new BigDecimal("8.5"), result);
        }

        @Test
        @DisplayName("negative Differenz: -7 - 3 ergibt -10")
        void subtractProducesNegative() {
            BigDecimal result = service.calculate(new BigDecimal("-7"), "SUBTRACT", new BigDecimal("3"));
            assertEquals(new BigDecimal("-10"), result);
        }
    }

    @Nested
    @DisplayName("MULTIPLY")
    class Multiply {
        @Test
        @DisplayName("12.5 * 4 ergibt 50")
        void multiplyBasic() {
            BigDecimal result = service.calculate(new BigDecimal("12.5"), "MULTIPLY", new BigDecimal("4"));
            assertEquals(new BigDecimal("50"), result);
        }

        @Test
        @DisplayName("sehr grosse Werte: 9999999999 * 9999999999")
        void multiplyVeryLarge() {
            BigDecimal result = service.calculate(new BigDecimal("9999999999"), "MULTIPLY", new BigDecimal("9999999999"));
            assertEquals(new BigDecimal("99999999980000000001"), result);
        }
    }

    @Nested
    @DisplayName("DIVIDE")
    class Divide {
        @Test
        @DisplayName("12.5 / 4 ergibt 3.125")
        void divideExact() {
            BigDecimal result = service.calculate(new BigDecimal("12.5"), "DIVIDE", new BigDecimal("4"));
            assertEquals(new BigDecimal("3.125"), result);
        }

        @Test
        @DisplayName("1 / 3 wird auf 10 Nachkommastellen kaufmaennisch gerundet")
        void divideRoundsToTenScale() {
            // 1/3 = 0.33333333333... -> auf 10 Stellen HALF_UP: 0.3333333333
            BigDecimal result = service.calculate(BigDecimal.ONE, "DIVIDE", new BigDecimal("3"));
            assertEquals(new BigDecimal("0.3333333333"), result);
        }

        @Test
        @DisplayName("Division durch null wirft DivisionByZeroException")
        void divideByZeroThrows() {
            assertThrows(DivisionByZeroException.class,
                    () -> service.calculate(BigDecimal.ONE, "DIVIDE", BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Division durch negative Zahl: 10 / -4 ergibt -2.5")
        void divideNegative() {
            BigDecimal result = service.calculate(new BigDecimal("10"), "DIVIDE", new BigDecimal("-4"));
            assertEquals(new BigDecimal("-2.5"), result);
        }
    }

    @Nested
    @DisplayName("PERCENT")
    class Percent {
        @Test
        @DisplayName("50 PERCENT 200 ergibt 100 (left * right / 100)")
        void percentBasic() {
            BigDecimal result = service.calculate(new BigDecimal("50"), "PERCENT", new BigDecimal("200"));
            assertEquals(new BigDecimal("100"), result);
        }

        @Test
        @DisplayName("10 PERCENT 25 ergibt 2.5")
        void percentDecimal() {
            BigDecimal result = service.calculate(new BigDecimal("10"), "PERCENT", new BigDecimal("25"));
            assertEquals(new BigDecimal("2.5"), result);
        }
    }

    @Nested
    @DisplayName("Operator-Parsing")
    class OperatorParsing {
        @Test
        @DisplayName("Kleinbuchstaben werden akzeptiert (case-insensitive)")
        void lowercaseOperatorAccepted() {
            BigDecimal result = service.calculate(new BigDecimal("2"), "add", new BigDecimal("3"));
            assertEquals(new BigDecimal("5"), result);
        }

        @Test
        @DisplayName("unbekannter Operator wirft UnknownOperatorException")
        void unknownOperatorThrows() {
            assertThrows(UnknownOperatorException.class,
                    () -> service.calculate(BigDecimal.ONE, "FOO", BigDecimal.ONE));
        }

        @Test
        @DisplayName("null-Operator wirft UnknownOperatorException")
        void nullOperatorThrows() {
            assertThrows(UnknownOperatorException.class,
                    () -> service.calculate(BigDecimal.ONE, null, BigDecimal.ONE));
        }

        @Test
        @DisplayName("leerer Operator wirft UnknownOperatorException")
        void blankOperatorThrows() {
            assertThrows(UnknownOperatorException.class,
                    () -> service.calculate(BigDecimal.ONE, "  ", BigDecimal.ONE));
        }
    }

    @Nested
    @DisplayName("Rundung und Notation")
    class Rounding {
        @Test
        @DisplayName("Ergebnis mit mehr als 10 Nachkommastellen wird gerundet")
        void roundsBeyondTenDecimals() {
            // 2 DIVIDE 3 = 0.66666666666... -> auf 10 Stellen HALF_UP: 0.6666666667
            BigDecimal result = service.calculate(new BigDecimal("2"), "DIVIDE", new BigDecimal("3"));
            assertEquals(new BigDecimal("0.6666666667"), result);
        }

        @Test
        @DisplayName("kaufmaennisches Runden (HALF_UP) an der 10. Nachkommastelle")
        void roundsHalfUp() {
            // 1 / 6 = 0.16666666666... -> 0.1666666667 (letzte 6 rundet auf)
            BigDecimal result = service.calculate(BigDecimal.ONE, "DIVIDE", new BigDecimal("6"));
            assertEquals(new BigDecimal("0.1666666667"), result);
        }

        @Test
        @DisplayName("ueberfluessige Nullen werden entfernt (3.125 statt 3.1250000000)")
        void stripsTrailingZeros() {
            BigDecimal result = service.calculate(new BigDecimal("12.5"), "DIVIDE", new BigDecimal("4"));
            assertEquals(3, result.scale());
        }

        @Test
        @DisplayName("ganzzahliges Ergebnis wird als normale Zahl ausgegeben (nicht 1E+2)")
        void integerResultNotScientificNotation() {
            BigDecimal result = service.calculate(new BigDecimal("2.5"), "ADD", new BigDecimal("3.5"));
            // toPlainString() stellt 100 statt 1E+2 dar; scale() == 0 bei ganzzahligen Werten
            assertEquals("6", result.toPlainString());
        }
    }
}
