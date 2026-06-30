package com.pathiful.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalculatorService.
 */
class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    // -----------------------------------------------------------------------
    // Basic arithmetic
    // -----------------------------------------------------------------------

    @Test
    void shouldAddTwoNumbers() {
        assertEquals(5.0, service.evaluate("2+3"), 0.001);
    }

    @Test
    void shouldSubtractTwoNumbers() {
        assertEquals(3.0, service.evaluate("7-4"), 0.001);
    }

    @Test
    void shouldMultiplyTwoNumbers() {
        assertEquals(12.0, service.evaluate("3*4"), 0.001);
    }

    @Test
    void shouldDivideTwoNumbers() {
        assertEquals(5.0, service.evaluate("10/2"), 0.001);
    }

    // -----------------------------------------------------------------------
    // Operator precedence (Punkt-vor-Strich)
    // -----------------------------------------------------------------------

    @Test
    void shouldRespectPrecedenceAdditionBeforeMultiplication() {
        assertEquals(14.0, service.evaluate("2+3*4"), 0.001);
    }

    @Test
    void shouldRespectPrecedenceMultiplicationBeforeAddition() {
        assertEquals(14.0, service.evaluate("3*4+2"), 0.001);
    }

    @Test
    void shouldRespectPrecedenceWithDivision() {
        assertEquals(12.0, service.evaluate("10+6/3"), 0.001);
    }

    @Test
    void shouldHandleComplexExpression() {
        assertEquals(20.0, service.evaluate("2+3*4+6"), 0.001);
    }

    // -----------------------------------------------------------------------
    // Negative numbers
    // -----------------------------------------------------------------------

    @Test
    void shouldHandleLeadingNegativeNumber() {
        assertEquals(-5.0, service.evaluate("-3-2"), 0.001);
    }

    @Test
    void shouldHandleNegativeAfterOperator() {
        assertEquals(1.0, service.evaluate("4+-3"), 0.001);
    }

    // -----------------------------------------------------------------------
    // Division by zero
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowOnDivisionByZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.evaluate("5/0"));
        assertEquals("Division by zero", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Invalid input
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowOnEmptyExpression() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluate(""));
    }

    @Test
    void shouldThrowOnNullExpression() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluate(null));
    }

    @Test
    void shouldThrowOnInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluate("2+abc"));
    }

    @Test
    void shouldThrowOnConsecutiveOperators() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluate("2++3"));
    }

    @Test
    void shouldThrowOnTrailingOperator() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluate("2+"));
    }

    // -----------------------------------------------------------------------
    // Whitespace handling
    // -----------------------------------------------------------------------

    @Test
    void shouldHandleWhitespace() {
        assertEquals(14.0, service.evaluate("2 + 3 * 4"), 0.001);
    }

    @Test
    void shouldHandleDecimalNumbers() {
        assertEquals(7.5, service.evaluate("3.5+4"), 0.001);
    }
}
