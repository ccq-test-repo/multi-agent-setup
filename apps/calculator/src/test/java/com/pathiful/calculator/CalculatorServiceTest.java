package com.pathiful.calculator;

import com.pathiful.calculator.service.CalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalculatorService.
 * Tests verify behaviour derived from the acceptance criteria, not implementation details.
 */
class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorService();
    }

    // -----------------------------------------------------------------------
    // Happy path: basic arithmetic
    // -----------------------------------------------------------------------

    @Test
    void shouldAddTwoNumbers() {
        assertEquals(5.0, service.calculate("2+3"), 0.0001);
    }

    @Test
    void shouldSubtractTwoNumbers() {
        assertEquals(3.0, service.calculate("7-4"), 0.0001);
    }

    @Test
    void shouldMultiplyTwoNumbers() {
        assertEquals(12.0, service.calculate("3*4"), 0.0001);
    }

    @Test
    void shouldDivideTwoNumbers() {
        assertEquals(5.0, service.calculate("10/2"), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Operator precedence (Punkt-vor-Strich)
    // -----------------------------------------------------------------------

    @Test
    void shouldRespectMultiplicationBeforeAddition() {
        assertEquals(14.0, service.calculate("2+3*4"), 0.0001);
    }

    @Test
    void shouldRespectMultiplicationBeforeAdditionReversed() {
        assertEquals(14.0, service.calculate("3*4+2"), 0.0001);
    }

    @Test
    void shouldRespectDivisionBeforeAddition() {
        assertEquals(12.0, service.calculate("10+6/3"), 0.0001);
    }

    @Test
    void shouldHandleMultipleOperators() {
        assertEquals(20.0, service.calculate("2+3*4+6"), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Parentheses
    // -----------------------------------------------------------------------

    @Test
    void shouldEvaluateParenthesesFirst() {
        assertEquals(20.0, service.calculate("(2+3)*4"), 0.0001);
    }

    @Test
    void shouldHandleNestedParentheses() {
        assertEquals(46.0, service.calculate("2*(3+4*5)"), 0.0001);
        // 2 * (3 + 20) = 2 * 23 = 46
    }

    @Test
    void shouldHandleDeeplyNestedParentheses() {
        assertEquals(13.0, service.calculate("(1+(2*3))+6"), 0.0001);
        // (1+6)+6 = 13
    }

    // -----------------------------------------------------------------------
    // Negative numbers
    // -----------------------------------------------------------------------

    @Test
    void shouldHandleLeadingNegativeNumber() {
        assertEquals(-5.0, service.calculate("-3-2"), 0.0001);
    }

    @Test
    void shouldHandleNegativeAfterOperator() {
        assertEquals(1.0, service.calculate("4+-3"), 0.0001);
    }

    @Test
    void shouldHandleNegativeInParentheses() {
        assertEquals(2.0, service.calculate("(-3)+5"), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Decimal numbers
    // -----------------------------------------------------------------------

    @Test
    void shouldAddDecimalNumbers() {
        assertEquals(7.5, service.calculate("3.5+4"), 0.0001);
    }

    @Test
    void shouldMultiplyDecimalNumbers() {
        assertEquals(8.75, service.calculate("2.5*3.5"), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Whitespace handling
    // -----------------------------------------------------------------------

    @Test
    void shouldHandleWhitespaceAroundOperators() {
        assertEquals(14.0, service.calculate("  2 + 3 * 4  "), 0.0001);
    }

    @Test
    void shouldHandleTabsAndWhitespace() {
        assertEquals(10.0, service.calculate("\t1\t+\t9\n"), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Error cases: division by zero
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowArithmeticExceptionOnDivisionByZero() {
        assertThrows(ArithmeticException.class, () -> service.calculate("5/0"));
    }

    @Test
    void shouldThrowOnDivisionByZeroInComplexExpression() {
        assertThrows(ArithmeticException.class, () -> service.calculate("2+5/0"));
    }

    // -----------------------------------------------------------------------
    // Error cases: invalid input
    // -----------------------------------------------------------------------

    @Test
    void shouldThrowOnNullExpression() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(null));
    }

    @Test
    void shouldThrowOnEmptyExpression() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(""));
    }

    @Test
    void shouldThrowOnBlankExpression() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate("   "));
    }

    @Test
    void shouldThrowOnInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate("2+abc"));
    }

    @Test
    void shouldHandleUnaryPlusAfterOperator() {
        // "2++3" is parsed as 2 + (+3) = 5, which is valid
        assertEquals(5.0, service.calculate("2++3"), 0.0001);
    }

    @Test
    void shouldThrowOnTrailingOperator() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate("2+"));
    }

    @Test
    void shouldThrowOnMissingClosingParenthesis() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate("(2+3"));
    }

    @Test
    void shouldThrowOnExtraClosingParenthesis() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate("2+3)"));
    }

    @Test
    void shouldThrowOnDivisionBySymbolCharacter() {
        // e.g. just a letter
        assertThrows(IllegalArgumentException.class, () -> service.calculate("x"));
    }

    // -----------------------------------------------------------------------
    // Additional edge cases
    // -----------------------------------------------------------------------

    @Test
    void shouldHandleSingleNumber() {
        assertEquals(42.0, service.calculate("42"), 0.0001);
    }

    @Test
    void shouldHandleSingleDecimal() {
        assertEquals(3.14, service.calculate("3.14"), 0.0001);
    }

    @Test
    void shouldHandleLeadingDecimal() {
        assertEquals(0.5, service.calculate(".5+0"), 0.0001);
    }

    @Test
    void shouldHandleUnaryPlus() {
        assertEquals(5.0, service.calculate("+5"), 0.0001);
    }

    @Test
    void shouldHandleChainedSubtraction() {
        assertEquals(-4.0, service.calculate("1-2-3"), 0.0001);
    }
}
