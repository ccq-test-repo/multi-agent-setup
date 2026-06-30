package com.pathiful.guestbook;

import com.pathiful.guestbook.model.GuestbookEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuestbookIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate rest;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        baseUrl = "http://localhost:" + port + "/api/messages";
    }

    @Test
    void shouldCreateMessage() {
        GuestbookEntry entry = postMessage("Alice", "Hallo");
        assertNotNull(entry.getId());
        assertEquals("Alice", entry.getAuthor());
        assertEquals("Hallo", entry.getText());
    }

    @Test
    void shouldListMessages() {
        postMessage("Alice", "Erster");
        postMessage("Bob", "Zweiter");

        ResponseEntity<GuestbookEntry[]> resp = rest.getForEntity(baseUrl, GuestbookEntry[].class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        GuestbookEntry[] entries = resp.getBody();
        assertNotNull(entries);
        assertTrue(entries.length >= 2);
        assertTrue(Arrays.stream(entries).anyMatch(e -> "Erster".equals(e.getText())));
        assertTrue(Arrays.stream(entries).anyMatch(e -> "Zweiter".equals(e.getText())));
    }

    @Test
    void shouldReturn400ForEmptyAuthor() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("author", "", "text", "Hallo")),
                    Void.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldReturn400ForEmptyText() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("author", "Alice", "text", "")),
                    Void.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldReturn400ForBothEmpty() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            rest.exchange(
                    RequestEntity
                            .post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("author", "", "text", "")),
                    Void.class
            );
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldReturn400ForMissingFields() {
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

    private GuestbookEntry postMessage(String author, String text) {
        ResponseEntity<GuestbookEntry> resp = rest.exchange(
                RequestEntity
                        .post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("author", author, "text", text)),
                GuestbookEntry.class
        );
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        return resp.getBody();
    }
}
