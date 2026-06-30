package com.pathiful.todo.service;

import com.pathiful.todo.model.Todo;
import com.pathiful.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodoService — verifies business logic in isolation
 * (repository is mocked).
 * <p>
 * These tests derive expected behaviour from the acceptance criteria,
 * not from the implementation internals.
 */
@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository repository;

    private TodoService service;

    @BeforeEach
    void setUp() {
        service = new TodoService(repository);
    }

    @Test
    void shouldCreateTodoWithTitle() {
        String title = "Milch kaufen";
        Todo todoToSave = new Todo(title);
        Todo saved = new Todo(title);
        saved.setId(1L);

        when(repository.save(any(Todo.class))).thenReturn(saved);

        Todo result = service.create(title);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(title, result.getTitle());
        assertFalse(result.isDone());
        verify(repository).save(any(Todo.class));
    }

    @Test
    void shouldListAllTodos() {
        Todo t1 = new Todo("Eins");
        t1.setId(1L);
        Todo t2 = new Todo("Zwei");
        t2.setId(2L);

        when(repository.findAll()).thenReturn(List.of(t1, t2));

        List<Todo> result = service.listAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void shouldFindByIdWhenExists() {
        Todo todo = new Todo("Test");
        todo.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(todo));

        Optional<Todo> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Test", result.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenFindByIdNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Todo> result = service.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldMarkDoneWhenExists() {
        Todo todo = new Todo("Erledigen");
        todo.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(todo));

        Optional<Todo> result = service.markDone(1L);

        assertTrue(result.isPresent());
        assertTrue(result.get().isDone());
    }

    @Test
    void shouldReturnEmptyWhenMarkDoneNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Todo> result = service.markDone(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldDeleteWhenExists() {
        when(repository.deleteById(1L)).thenReturn(true);

        boolean deleted = service.deleteById(1L);

        assertTrue(deleted);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldReturnFalseWhenDeleteNotFound() {
        when(repository.deleteById(999L)).thenReturn(false);

        boolean deleted = service.deleteById(999L);

        assertFalse(deleted);
        verify(repository).deleteById(999L);
    }
}
