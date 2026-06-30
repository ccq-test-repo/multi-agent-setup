package com.pathiful.calculator;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-DTO for POST /api/calculator/calculate.
 */
public class CalculatorRequest {

    @NotBlank(message = "Expression is required")
    private String expression;

    public CalculatorRequest() {}

    public CalculatorRequest(String expression) {
        this.expression = expression;
    }

    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
}
