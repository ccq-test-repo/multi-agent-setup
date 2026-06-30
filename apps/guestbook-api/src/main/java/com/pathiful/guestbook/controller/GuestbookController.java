package com.pathiful.guestbook.controller;

import com.pathiful.guestbook.model.CreateMessageRequest;
import com.pathiful.guestbook.model.ErrorResponse;
import com.pathiful.guestbook.model.GuestbookEntry;
import com.pathiful.guestbook.repository.InMemoryGuestbookRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class GuestbookController {

    private final InMemoryGuestbookRepository repository;

    public GuestbookController(InMemoryGuestbookRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<GuestbookEntry> listMessages() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<GuestbookEntry> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        GuestbookEntry entry = repository.save(request.getAuthor(), request.getText());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }
}
