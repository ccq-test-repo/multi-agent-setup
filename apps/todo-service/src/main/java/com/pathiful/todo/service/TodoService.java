package com.pathiful.todo.service;

import com.pathiful.todo.model.Todo;
import com.pathiful.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for To-Do operations.
 */
@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public Todo create(String title) {
        Todo todo = new Todo(title);
        return repository.save(todo);
    }

    public List<Todo> listAll() {
        return repository.findAll();
    }

    public Optional<Todo> findById(long id) {
        return repository.findById(id);
    }

    public Optional<Todo> markDone(long id) {
        return repository.findById(id).map(todo -> {
            todo.setDone(true);
            return todo;
        });
    }

    public boolean deleteById(long id) {
        return repository.deleteById(id);
    }
}
