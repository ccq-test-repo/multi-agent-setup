package com.pathiful.calculator;

/**
 * Response-DTO for POST /api/calculator/calculate.
 */
public class CalculatorResponse {

    private double result;

    public CalculatorResponse() {}

    public CalculatorResponse(double result) {
        this.result = result;
    }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }
}
