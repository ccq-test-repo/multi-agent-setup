package com.pathiful.calculator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for the calc endpoints (/api/calc/*).
 *
 * POST /api/calc/multiply
 */
@RestController
@RequestMapping("/api/calc")
public class CalcController {

    /**
     * POST /api/calc/multiply
     *
     * Body: { "a": number, "b": number }
     * Response (200): { "result": number }
     * Error (400):     { "error": "invalid_input" }
     */
    @PostMapping("/multiply")
    public ResponseEntity<?> multiply(@RequestBody MultiplyRequest request) {
        Double a = request.getA();
        Double b = request.getB();

        if (a == null || b == null || Double.isNaN(a) || Double.isNaN(b)
                || Double.isInfinite(a) || Double.isInfinite(b)) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_input"));
        }

        double result = a * b;
        return ResponseEntity.ok(new MultiplyResponse(result));
    }
}
