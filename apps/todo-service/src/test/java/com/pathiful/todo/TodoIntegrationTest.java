package com.pathiful.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathiful.todo.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration tests for the To-Do REST service.
 * Starts the embedded server on a random port and exercises all endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private RestTemplate rest;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        baseUrl = "http://localhost:" + port + "/api/todos";
    }

    @Test
    void shouldCreateTodo() {
        Todo created = postTodo("Milch kaufen");
        assertNotNull(created.getId());
        assertEquals("Milch kaufen", created.getTitle());
        assertFalse(created.isDone());
    }

    @Test
    void shouldListTodos() {
        postTodo("Eins");
        postTodo("Zwei");

        Todo[] todos = rest.getForObject(baseUrl, Todo[].class);
        assertNotNull(todos);
        assertTrue(todos.length >= 2);
        assertTrue(Arrays.stream(todos).anyMatch(t -> "Eins".equals(t.getTitle())));
        assertTrue(Arrays.stream(todos).anyMatch(t -> "Zwei".equals(t.getTitle())));
    }

    @Test
    void shouldGetById() {
        Todo created = postTodo("Test-Todo");
        Todo fetched = rest.getForObject(baseUrl + "/" + created.getId(), Todo.class);
        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());
        assertEquals(created.getTitle(), fetched.getTitle());
    }

    @Test
    void shouldReturn404ForUnknownId() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.getForEntity(baseUrl + "/99999", Todo.class);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldMarkDone() {
        Todo created = postTodo("Erledigen");
        Todo done = rest.exchange(
                RequestEntity.put(baseUrl + "/" + created.getId() + "/done").build(),
                Todo.class
        ).getBody();
        assertNotNull(done);
        assertTrue(done.isDone());
    }

    @Test
    void shouldReturn404ForMarkDoneOnUnknownId() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity.put(baseUrl + "/99999/done").build(),
                    Todo.class
            );
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldDelete() {
        Todo created = postTodo("Löschen");
        ResponseEntity<Void> deleteResp = rest.exchange(
                RequestEntity.delete(baseUrl + "/" + created.getId()).build(),
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());

        // verify gone
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.getForEntity(baseUrl + "/" + created.getId(), Todo.class);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldReturn404ForDeleteOnUnknownId() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity.delete(baseUrl + "/99999").build(),
                    Void.class
            );
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldReturn400ForBlankTitle() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("title", "")),
                    Void.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldReturn400ForMissingTitle() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of()),
                    Void.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- helpers ---

    private Todo postTodo(String title) {
        ResponseEntity<Todo> resp = rest.exchange(
                RequestEntity
                        .post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("title", title)),
                Todo.class
        );
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        return resp.getBody();
    }
}
