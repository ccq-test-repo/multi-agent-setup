package de.pathiful.calculator.api;

import java.math.BigDecimal;
import java.util.Map;

import de.pathiful.calculator.model.CalculationRequest;
import de.pathiful.calculator.model.CalculationResponse;
import de.pathiful.calculator.service.CalculatorService;
import de.pathiful.calculator.service.DivisionByZeroException;
import de.pathiful.calculator.service.UnknownOperatorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller des Taschenrechners.
 * <p>
 * Nimmt Anfragen entgegen, validiert und gibt Ergebnisse zurueck. Die Rechenlogik
 * liegt vollstaendig im {@link CalculatorService} — der Controller rechnet nicht.
 */
@RestController
public class CalculatorController {

    private final CalculatorService service;

    public CalculatorController(CalculatorService service) {
        this.service = service;
    }

    @PostMapping("/api/calculate")
    public ResponseEntity<?> calculate(@RequestBody CalculationRequest request) {
        if (isInvalid(request)) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Ungueltige Anfrage: Felder 'left', 'operator' und 'right' sind erforderlich.");
        }
        try {
            BigDecimal result = service.calculate(request.left(), request.operator(), request.right());
            return ResponseEntity.ok(new CalculationResponse(result));
        } catch (DivisionByZeroException e) {
            return error(HttpStatus.BAD_REQUEST, "DIVISION_BY_ZERO", "Division durch null ist nicht definiert.");
        } catch (UnknownOperatorException e) {
            return error(HttpStatus.BAD_REQUEST, "UNKNOWN_OPERATOR", "Unbekannter Operator.");
        }
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    private boolean isInvalid(CalculationRequest request) {
        if (request == null || request.left() == null || request.right() == null) {
            return true;
        }
        String operator = request.operator();
        return operator == null || operator.isBlank();
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of("error", error, "message", message));
    }
}
