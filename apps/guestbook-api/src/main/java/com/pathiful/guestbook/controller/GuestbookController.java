package com.pathiful.guestbook.controller;

import com.pathiful.guestbook.model.CreateMessageRequest;
import com.pathiful.guestbook.model.ErrorResponse;
import com.pathiful.guestbook.model.GuestbookEntry;
import com.pathiful.guestbook.service.GuestbookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for guestbook messages.
 */
@RestController
@RequestMapping("/api/messages")
public class GuestbookController {

    private final GuestbookService guestbookService;

    public GuestbookController(GuestbookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    @GetMapping
    public ResponseEntity<List<GuestbookEntry>> listMessages() {
        return ResponseEntity.ok(guestbookService.listAll());
    }

    @PostMapping
    public ResponseEntity<GuestbookEntry> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        GuestbookEntry created = guestbookService.create(request.getAuthor(), request.getText());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
}
