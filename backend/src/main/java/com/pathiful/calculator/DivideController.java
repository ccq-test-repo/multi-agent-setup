package com.pathiful.calculator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for the dedicated division endpoint.
 *
 * POST /api/calc/divide
 */
@RestController
@RequestMapping("/api/calc")
public class DivideController {

    /**
     * POST /api/calc/divide
     *
     * Request:  { "a": number, "b": number }
     * Response: { "result": number }  →  a / b
     *
     * Errors (HTTP 400):
     *   { "error": "invalid_input" }     when a or b is missing, not a finite number,
     *                                    or the request body is malformed
     *   { "error": "division_by_zero" }  when b == 0
     */
    @PostMapping("/divide")
    public ResponseEntity<?> divide(@RequestBody DivideRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_input"));
        }

        Double a = request.getA();
        Double b = request.getB();

        // Validate both values are present and finite
        if (a == null || b == null || Double.isNaN(a) || Double.isNaN(b)
                || Double.isInfinite(a) || Double.isInfinite(b)) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_input"));
        }

        // Division by zero
        if (b == 0.0) {
            return ResponseEntity.badRequest().body(Map.of("error", "division_by_zero"));
        }

        double result = a / b;
        return ResponseEntity.ok(new DivideResponse(result));
    }
}
