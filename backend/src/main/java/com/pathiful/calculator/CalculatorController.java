package com.pathiful.calculator;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
     * On error (division by zero, invalid expression) returns HTTP 400
     * with { "error": "..." }.
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@Valid @RequestBody CalculatorRequest request) {
        try {
            double result = calculatorService.evaluate(request.getExpression());
            return ResponseEntity.ok(new CalculatorResponse(result));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            // Map specific error messages
            if ("Division by zero".equals(msg)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Division by zero"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid expression"));
        }
    }
}
