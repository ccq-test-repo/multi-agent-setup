package de.pathiful.calculator.model;

import java.math.BigDecimal;

/**
 * Eingabemodell fuer {@code POST /api/calculate}.
 *
 * @param left     linker Operand
 * @param operator Operator als String ({@code ADD}, {@code SUBTRACT}, {@code MULTIPLY},
 *                 {@code DIVIDE}, {@code PERCENT})
 * @param right    rechter Operand
 */
public record CalculationRequest(BigDecimal left, String operator, BigDecimal right) {
}
