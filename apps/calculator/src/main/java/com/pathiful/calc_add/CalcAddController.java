package com.pathiful.calc_add;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for the simple addition endpoint.
 *
 * POST /api/calc/add
 */
@RestController
@RequestMapping("/api/calc")
public class CalcAddController {

    /**
     * POST /api/calc/add
     *
     * Body: { "a": 3, "b": 5 }
     * Response (200): { "result": 8.0 }
     *
     * On invalid/non-numeric input the handler returns HTTP 400
     * with { "error": "invalid_input" }.
     */
    @PostMapping("/add")
    public ResponseEntity<CalcAddResponse> add(@Valid @RequestBody CalcAddRequest request) {
        double result = request.getA() + request.getB();
        return ResponseEntity.ok(new CalcAddResponse(result));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_input"));
    }
}
