package com.pathiful.calculator.model;

import jakarta.validation.constraints.NotNull;

/**
 * Request-DTO for POST /api/calc/subtract.
 */
public class CalcSubtractRequest {

    @NotNull(message = "a must not be null")
    private Double a;

    @NotNull(message = "b must not be null")
    private Double b;

    public CalcSubtractRequest() {
    }

    public CalcSubtractRequest(Double a, Double b) {
        this.a = a;
        this.b = b;
    }

    public Double getA() {
        return a;
    }

    public void setA(Double a) {
        this.a = a;
    }

    public Double getB() {
        return b;
    }

    public void setB(Double b) {
        this.b = b;
    }
}
