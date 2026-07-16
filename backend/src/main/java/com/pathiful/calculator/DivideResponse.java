package com.pathiful.calculator;

/**
 * Success response-DTO for POST /api/calc/divide.
 */
public class DivideResponse {

    private double result;

    public DivideResponse() {}

    public DivideResponse(double result) {
        this.result = result;
    }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }
}
