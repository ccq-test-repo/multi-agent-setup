package com.pathiful.calculator.controller.calc;

import com.pathiful.calculator.model.CalcAddRequest;
import com.pathiful.calculator.model.CalcAddResponse;
import com.pathiful.calculator.model.ErrorResponse;
import com.pathiful.calculator.service.calc.CalcAddService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the simple addition endpoint.
 *
 * POST /api/calc/add
 */
@RestController
@RequestMapping("/api/calc")
public class CalcAddController {

    private final CalcAddService calcAddService;

    public CalcAddController(CalcAddService calcAddService) {
        this.calcAddService = calcAddService;
    }

    /**
     * POST /api/calc/add
     *
     * Body: { "a": 3, "b": 5 }
     * Response (200): { "result": 8.0 }
     *
     * On invalid/non-numeric input returns HTTP 400
     * with { "error": "invalid_input" }.
     */
    @PostMapping("/add")
    public ResponseEntity<CalcAddResponse> add(@Valid @RequestBody CalcAddRequest request) {
        double result = calcAddService.add(request.getA(), request.getB());
        return ResponseEntity.ok(new CalcAddResponse(result));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("invalid_input"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("invalid_input"));
    }
}
