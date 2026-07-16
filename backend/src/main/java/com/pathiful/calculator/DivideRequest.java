package com.pathiful.calculator;

/**
 * Request-DTO for POST /api/calc/divide.
 */
public class DivideRequest {

    private Double a;
    private Double b;

    public DivideRequest() {}

    public DivideRequest(Double a, Double b) {
        this.a = a;
        this.b = b;
    }

    public Double getA() { return a; }
    public void setA(Double a) { this.a = a; }

    public Double getB() { return b; }
    public void setB(Double b) { this.b = b; }
}
