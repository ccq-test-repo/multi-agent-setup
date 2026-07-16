package com.pathiful.calculator;

/**
 * Response-DTO for POST /api/calc/multiply.
 */
public class MultiplyResponse {

    private double result;

    public MultiplyResponse() {}

    public MultiplyResponse(double result) {
        this.result = result;
    }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }
}
