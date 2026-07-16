package com.pathiful.calc_subtract;

/**
 * Response-DTO for POST /api/calc/subtract.
 */
public class CalcSubtractResponse {

    private double result;

    public CalcSubtractResponse() {
    }

    public CalcSubtractResponse(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}
