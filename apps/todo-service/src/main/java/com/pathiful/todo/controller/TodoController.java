package com.pathiful.todo.controller;

import com.pathiful.todo.model.CreateTodoRequest;
import com.pathiful.todo.model.ErrorResponse;
import com.pathiful.todo.model.Todo;
import com.pathiful.todo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for To-Do operations.
 */
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<Todo> create(@Valid @RequestBody CreateTodoRequest request) {
        Todo created = todoService.create(request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Todo>> list() {
        return ResponseEntity.ok(todoService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getById(@PathVariable long id) {
        return todoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/done")
    public ResponseEntity<Todo> markDone(@PathVariable long id) {
        return todoService.markDone(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (todoService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("title must not be blank");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
}
