package com.pathiful.calculator;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the calculator endpoint.
 *
 * POST /api/calculator/calculate
 */
@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * POST /api/calculator/calculate
     *
     * Body: { "expression": "2+3*4" }
     * Response: { "result": 14.0 }
     *
     * On error (division by zero, invalid expression) the
     * {@link com.pathiful.common.GlobalExceptionHandler} returns HTTP 400
     * with the standard {@link com.pathiful.common.ApiError} response body.
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculatorResponse> calculate(@Valid @RequestBody CalculatorRequest request) {
        double result = calculatorService.evaluate(request.getExpression());
        return ResponseEntity.ok(new CalculatorResponse(result));
    }
}
