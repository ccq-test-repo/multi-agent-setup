package com.pathiful.feedback.controller;

import com.pathiful.feedback.model.CreateFeedbackRequest;
import com.pathiful.feedback.model.ErrorResponse;
import com.pathiful.feedback.model.FeedbackEntry;
import com.pathiful.feedback.repository.InMemoryFeedbackRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Endpunkte des Feedback-Boards (Issue #55).
 *
 * <pre>
 *   GET  /feedback -> 200 mit Liste aller Einträge
 *   POST /feedback -> 201 mit gespeichertem Eintrag, bei ungültigen Eingaben 400
 * </pre>
 *
 * Fehlerantworten nutzen das einheitliche {@link ErrorResponse}-Format; es werden
 * keine internen Details oder Stacktraces an den Client ausgegeben.
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final InMemoryFeedbackRepository repository;

    public FeedbackController(InMemoryFeedbackRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<FeedbackEntry> listFeedback() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<FeedbackEntry> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        FeedbackEntry entry = repository.save(
                request.getTitle(),
                request.getDescription(),
                request.getCategory()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    /** Bean-Validierungsfehler (@Valid) -> 400 mit verständlicher Meldung. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }

    /** Unlesbarer Body / ungültige Kategorie-Werte -> 400 statt 500. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid or malformed request body"));
    }
}
