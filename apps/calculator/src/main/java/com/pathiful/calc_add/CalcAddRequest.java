package com.pathiful.calc_add;

import jakarta.validation.constraints.NotNull;

/**
 * Request-DTO for POST /api/calc/add.
 */
public class CalcAddRequest {

    @NotNull(message = "a must not be null")
    private Double a;

    @NotNull(message = "b must not be null")
    private Double b;

    public CalcAddRequest() {
    }

    public CalcAddRequest(Double a, Double b) {
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
