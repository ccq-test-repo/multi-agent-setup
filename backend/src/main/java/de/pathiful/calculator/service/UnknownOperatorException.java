package de.pathiful.calculator.service;

/**
 * Wird geworfen, wenn ein unbekannter Operator uebergeben wird.
 */
public class UnknownOperatorException extends RuntimeException {

    public UnknownOperatorException(String operator) {
        super("Unbekannter Operator: " + operator);
    }
}
