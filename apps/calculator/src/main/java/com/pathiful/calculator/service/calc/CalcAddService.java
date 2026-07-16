package com.pathiful.calculator.service.calc;

import org.springframework.stereotype.Service;

/**
 * Service for the simple addition endpoint.
 */
@Service
public class CalcAddService {

    /**
     * Adds two numbers.
     *
     * @param a first addend
     * @param b second addend
     * @return a + b
     */
    public double add(double a, double b) {
        return a + b;
    }
}
