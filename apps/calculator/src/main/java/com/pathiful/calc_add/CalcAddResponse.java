package com.pathiful.calc_add;

/**
 * Response-DTO for POST /api/calc/add.
 */
public class CalcAddResponse {

    private double result;

    public CalcAddResponse() {
    }

    public CalcAddResponse(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}
