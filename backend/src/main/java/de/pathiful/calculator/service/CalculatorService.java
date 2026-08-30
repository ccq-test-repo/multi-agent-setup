package de.pathiful.calculator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Enthaelt die gesamte Rechenlogik des Taschenrechners.
 * <p>
 * Dieses POJO kennt weder HTTP noch Spring-Web-Annotationen und ist daher ohne
 * laufenden Server direkt testbar. Es rechnet ausschliesslich mit
 * {@link BigDecimal} aus der Standardbibliothek.
 */
public class CalculatorService {

    /** Die unterstuetzten Rechenoperationen. */
    public enum Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, PERCENT
    }

    /** Auf diese Nachkommastellen wird das Ergebnis kaufmaennisch gerundet. */
    private static final int SCALE = 10;

    /** Kaufmaennisch runden (halbe Werte werden aufgerundet). */
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Fuehrt die Rechenoperation aus.
     *
     * @param left     linker Operand
     * @param operator Operator-String (siehe {@link Operator})
     * @param right    rechter Operand
     * @return das auf {@value SCALE} Nachkommastellen gerundete Ergebnis ohne
     *         ueberfluessige Nullen
     * @throws UnknownOperatorException bei unbekanntem Operator
     * @throws DivisionByZeroException  bei Division durch null
     */
    public BigDecimal calculate(BigDecimal left, String operator, BigDecimal right) {
        Operator op = parseOperator(operator);
        return switch (op) {
            case ADD -> round(left.add(right));
            case SUBTRACT -> round(left.subtract(right));
            case MULTIPLY -> round(left.multiply(right));
            case DIVIDE -> divide(left, right);
            case PERCENT -> round(left.multiply(right).divide(BigDecimal.valueOf(100), SCALE, ROUNDING));
        };
    }

    private Operator parseOperator(String operator) {
        try {
            return Operator.valueOf(operator.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new UnknownOperatorException(operator);
        }
    }

    private BigDecimal divide(BigDecimal left, BigDecimal right) {
        if (right.compareTo(BigDecimal.ZERO) == 0) {
            throw new DivisionByZeroException();
        }
        return round(left.divide(right, SCALE, ROUNDING));
    }

    private BigDecimal round(BigDecimal value) {
        BigDecimal rounded = value.setScale(SCALE, ROUNDING).stripTrailingZeros();
        // Ganzzahlige Werte wie 1E+2 wieder als 100 ausgeben statt in wissenschaftlicher Notation.
        if (rounded.scale() < 0) {
            rounded = rounded.setScale(0);
        }
        return rounded;
    }
}
