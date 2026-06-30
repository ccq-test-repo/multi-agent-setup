package com.pathiful.calculator.model;

import jakarta.validation.constraints.NotBlank;

public class CalculateRequest {

    @NotBlank(message = "expression must not be blank")
    private String expression;

    public CalculateRequest() {
    }

    public CalculateRequest(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
