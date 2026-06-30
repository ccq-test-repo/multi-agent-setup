package com.pathiful.guestbook;

import com.pathiful.guestbook.model.CreateMessageRequest;
import com.pathiful.guestbook.model.ErrorResponse;
import com.pathiful.guestbook.model.GuestbookEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Guestbook REST API.
 * Derived from acceptance criteria; tests verify behaviour, not implementation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuestbookApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String messagesUrl;

    @BeforeEach
    void setUp() {
        messagesUrl = "http://localhost:" + port + "/api/messages";
    }

    /** AC: POST /api/messages with valid {author, text} → 201 + entry with id */
    @Test
    void shouldCreateMessageAndReturn201() {
        var request = new CreateMessageRequest("Alice", "Hallo");

        ResponseEntity<GuestbookEntry> response = rest.postForEntity(
                messagesUrl, request, GuestbookEntry.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        GuestbookEntry entry = response.getBody();
        assertNotNull(entry, "Response body must contain the created entry");
        assertNotNull(entry.getId(), "Created entry must have an id");
        assertEquals("Alice", entry.getAuthor());
        assertEquals("Hallo", entry.getText());
    }

    /** AC: GET /api/messages → 200 + list containing previously created messages */
    @Test
    void shouldListAllMessages() {
        rest.postForEntity(messagesUrl, new CreateMessageRequest("Alice", "Hello"), GuestbookEntry.class);
        rest.postForEntity(messagesUrl, new CreateMessageRequest("Bob", "World"), GuestbookEntry.class);

        ResponseEntity<GuestbookEntry[]> response = rest.getForEntity(messagesUrl, GuestbookEntry[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GuestbookEntry[] entries = response.getBody();
        assertNotNull(entries);
        assertTrue(entries.length >= 2, "Should contain at least the two posted messages");
        boolean hasHello = false;
        boolean hasWorld = false;
        for (GuestbookEntry e : entries) {
            if ("Hello".equals(e.getText())) hasHello = true;
            if ("World".equals(e.getText())) hasWorld = true;
        }
        assertTrue(hasHello, "List must contain 'Hello'");
        assertTrue(hasWorld, "List must contain 'World'");
    }

    /** AC: POST with empty author → 400 */
    @Test
    void shouldRejectEmptyAuthor() {
        var request = new CreateMessageRequest("", "Hallo");

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                messagesUrl, request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
    }

    /** AC: POST with empty text → 400 */
    @Test
    void shouldRejectEmptyText() {
        var request = new CreateMessageRequest("Alice", "");

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                messagesUrl, request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
    }

    /** AC: POST with both fields empty → 400 */
    @Test
    void shouldRejectBothEmpty() {
        var request = new CreateMessageRequest("", "");

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                messagesUrl, request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** Edge: POST with null body → 400 */
    @Test
    void shouldRejectNullBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ErrorResponse> response = rest.exchange(
                messagesUrl, HttpMethod.POST, entity, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** Edge: POST with completely missing fields → 400 */
    @Test
    void shouldRejectMissingFields() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<ErrorResponse> response = rest.exchange(
                messagesUrl, HttpMethod.POST, entity, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** Edge: whitespace-only fields → 400 (since @NotBlank) */
    @Test
    void shouldRejectBlankFields() {
        var request = new CreateMessageRequest("   ", "   ");

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                messagesUrl, request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
