package com.pathiful.todo.repository;

import com.pathiful.todo.model.Todo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory To-Do store.
 */
@Repository
public class InMemoryTodoRepository implements TodoRepository {

    private final ConcurrentHashMap<Long, Todo> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Override
    public Todo save(Todo todo) {
        if (todo.getId() == 0) {
            todo.setId(idSeq.getAndIncrement());
        }
        store.put(todo.getId(), todo);
        return todo;
    }

    @Override
    public List<Todo> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Todo> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean deleteById(long id) {
        return store.remove(id) != null;
    }

    @Override
    public long nextId() {
        return idSeq.getAndIncrement();
    }
}
