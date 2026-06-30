package com.pathiful.todo.repository;

import com.pathiful.todo.model.Todo;

import java.util.List;
import java.util.Optional;

/**
 * In-memory store for To-Dos.
 */
public interface TodoRepository {

    Todo save(Todo todo);

    List<Todo> findAll();

    Optional<Todo> findById(long id);

    boolean deleteById(long id);

    long nextId();
}
