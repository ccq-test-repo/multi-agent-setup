package com.pathiful.calculator.controller;

import com.pathiful.calculator.model.CalcSubtractRequest;
import com.pathiful.calculator.model.CalcSubtractResponse;
import com.pathiful.calculator.model.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the simple subtraction endpoint.
 *
 * POST /api/calc/subtract
 */
@RestController
@RequestMapping("/api/calc")
public class CalcSubtractController {

    /**
     * POST /api/calc/subtract
     *
     * Body: { "a": 5, "b": 3 }
     * Response (200): { "result": 2.0 }
     *
     * On invalid/non-numeric input the handler returns HTTP 400
     * with { "error": "invalid_input" }.
     */
    @PostMapping("/subtract")
    public ResponseEntity<CalcSubtractResponse> subtract(@Valid @RequestBody CalcSubtractRequest request) {
        double result = request.getA() - request.getB();
        return ResponseEntity.ok(new CalcSubtractResponse(result));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("invalid_input"));
    }
}
