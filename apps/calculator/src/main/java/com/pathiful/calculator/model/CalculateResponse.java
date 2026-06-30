package com.pathiful.calculator.model;

public class CalculateResponse {

    private double result;

    public CalculateResponse() {
    }

    public CalculateResponse(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}
