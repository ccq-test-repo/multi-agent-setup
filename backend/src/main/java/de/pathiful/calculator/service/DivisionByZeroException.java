package de.pathiful.calculator.service;

/**
 * Wird geworfen, wenn eine Division durch null versucht wird.
 */
public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException() {
        super("Division durch null ist nicht definiert.");
    }
}
