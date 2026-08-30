package de.pathiful.calculator.model;

import java.math.BigDecimal;

/**
 * Ausgabemodell fuer {@code POST /api/calculate}.
 *
 * @param result das berechnete Ergebnis als BigDecimal
 */
public record CalculationResponse(BigDecimal result) {
}
