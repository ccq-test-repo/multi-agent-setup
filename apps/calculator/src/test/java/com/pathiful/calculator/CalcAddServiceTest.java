package com.pathiful.calculator;

import com.pathiful.calculator.service.calc.CalcAddService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalcAddService.
 *
 * Tests verify the addition behaviour derived from the acceptance criteria.
 */
class CalcAddServiceTest {

    private CalcAddService service;

    @BeforeEach
    void setUp() {
        service = new CalcAddService();
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void shouldAddTwoPositiveIntegers() {
        assertEquals(5.0, service.add(2.0, 3.0), 0.0001);
    }

    @Test
    void shouldAddPositiveAndNegativeNumber() {
        assertEquals(1.0, service.add(5.0, -4.0), 0.0001);
    }

    @Test
    void shouldAddTwoNegativeNumbers() {
        assertEquals(-8.0, service.add(-5.0, -3.0), 0.0001);
    }

    @Test
    void shouldAddZeroToNumber() {
        assertEquals(7.0, service.add(7.0, 0.0), 0.0001);
    }

    @Test
    void shouldAddTwoZeros() {
        assertEquals(0.0, service.add(0.0, 0.0), 0.0001);
    }

    @Test
    void shouldAddDecimalNumbers() {
        assertEquals(6.2, service.add(2.5, 3.7), 0.0001);
    }

    @Test
    void shouldAddLargeNumbers() {
        assertEquals(1000000.0, service.add(400000.0, 600000.0), 0.0001);
    }

    @Test
    void shouldAddNegativeDecimal() {
        assertEquals(-1.5, service.add(2.5, -4.0), 0.0001);
    }

    @Test
    void shouldHandleFloatingPointPrecision() {
        // Basic floating-point addition
        double result = service.add(0.1, 0.2);
        assertTrue(Math.abs(result - 0.3) < 0.0001);
    }
}
