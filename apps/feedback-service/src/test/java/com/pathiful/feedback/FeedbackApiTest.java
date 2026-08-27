package com.pathiful.feedback;

import com.pathiful.feedback.model.CreateFeedbackRequest;
import com.pathiful.feedback.model.ErrorResponse;
import com.pathiful.feedback.model.FeedbackCategory;
import com.pathiful.feedback.model.FeedbackEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API-Integrationstests fuer das Feedback-Board (Issue #55).
 *
 * Abgeleitet aus den Akzeptanzkriterien:
 *  - GET /feedback liefert alle vorhandenen Eintraege.
 *  - POST /feedback validiert serverseitig und speichert gueltige Eintraege (201).
 *  - Ungueltige/leere Eingaben werden serverseitig abgewiesen (4xx, verstaendliche Meldung).
 *  - Kein Stacktrace / keine internen Details an den Client.
 *
 * Bewusstes Muster: TestRestTemplate + @SpringBootTest(RANDOM_PORT), konsistent mit
 * den Schwester-Services (guestbook-api, todo-service). Es wird Verhalten getestet,
 * nicht die interne Implementierung.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeedbackApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String feedbackUrl() {
        return "http://localhost:" + port + "/feedback";
    }

    // --- Happy Path ----------------------------------------------------------

    /** AC: POST /feedback mit gueltigem Body -> 201 mit gespeichertem Eintrag. */
    @Test
    void shouldCreateFeedbackAndReturn201() {
        var request = new CreateFeedbackRequest(
                "Titel A", "Beschreibung A", FeedbackCategory.Idee);

        ResponseEntity<FeedbackEntry> response = rest.postForEntity(
                feedbackUrl(), request, FeedbackEntry.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        FeedbackEntry entry = response.getBody();
        assertNotNull(entry, "Response body must contain the created entry");
        assertNotNull(entry.getId(), "Created entry must have an id");
        assertEquals("Titel A", entry.getTitle());
        assertEquals("Beschreibung A", entry.getDescription());
        assertEquals(FeedbackCategory.Idee, entry.getCategory());
        assertNotNull(entry.getCreatedAt(), "Created entry must have a createdAt timestamp");
    }

    /** AC: POST /feedback erzeugt Eintrag, auch ohne optionale Beschreibung. */
    @Test
    void shouldCreateFeedbackWithoutDescription() {
        var request = new CreateFeedbackRequest(
                "Nur Titel", null, FeedbackCategory.Bug);

        ResponseEntity<FeedbackEntry> response = rest.postForEntity(
                feedbackUrl(), request, FeedbackEntry.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        FeedbackEntry entry = response.getBody();
        assertNotNull(entry);
        assertEquals("Nur Titel", entry.getTitle());
        assertNull(entry.getDescription(), "Description should be null when omitted");
        assertEquals(FeedbackCategory.Bug, entry.getCategory());
    }

    /** AC: GET /feedback liefert alle zuvor erstellten Eintraege. */
    @Test
    void shouldListAllCreatedEntries() {
        rest.postForEntity(feedbackUrl(),
                new CreateFeedbackRequest("Eins", "d1", FeedbackCategory.Idee),
                FeedbackEntry.class);
        rest.postForEntity(feedbackUrl(),
                new CreateFeedbackRequest("Zwei", "d2", FeedbackCategory.Sonstiges),
                FeedbackEntry.class);

        ResponseEntity<FeedbackEntry[]> response =
                rest.getForEntity(feedbackUrl(), FeedbackEntry[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        FeedbackEntry[] entries = response.getBody();
        assertNotNull(entries, "Response body must be a JSON array");
        boolean hasEins = false;
        boolean hasZwei = false;
        for (FeedbackEntry e : entries) {
            if ("Eins".equals(e.getTitle())) hasEins = true;
            if ("Zwei".equals(e.getTitle())) hasZwei = true;
        }
        assertTrue(hasEins, "List must contain 'Eins'");
        assertTrue(hasZwei, "List must contain 'Zwei'");
    }

    /** AC: GET /feedback liefert leer erstellten Eintraegen auch neue; aufsteigend nach id. */
    @Test
    void shouldIncrementIdsAcrossCreations() {
        ResponseEntity<FeedbackEntry> first = rest.postForEntity(feedbackUrl(),
                new CreateFeedbackRequest("erster", null, FeedbackCategory.Bug),
                FeedbackEntry.class);
        ResponseEntity<FeedbackEntry> second = rest.postForEntity(feedbackUrl(),
                new CreateFeedbackRequest("zweiter", null, FeedbackCategory.Idee),
                FeedbackEntry.class);

        assertEquals(HttpStatus.CREATED, first.getStatusCode());
        assertEquals(HttpStatus.CREATED, second.getStatusCode());
        assertNotNull(first.getBody());
        assertNotNull(second.getBody());
        assertTrue(second.getBody().getId() > first.getBody().getId(),
                "Each new entry must receive a new (higher) id");
    }

    // --- Fehlerfaelle (Validierung) -----------------------------------------

    /** AC: POST mit leerem Titel -> 400 mit verstaendlicher Meldung. */
    @Test
    void shouldRejectBlankTitle() {
        var request = new CreateFeedbackRequest(
                "   ", "Beschreibung", FeedbackCategory.Idee);

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                feedbackUrl(), request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertValidationMessage(response, "title");
    }

    /** AC: POST mit fehlender Kategorie -> 400. */
    @Test
    void shouldRejectMissingCategory() {
        var request = new CreateFeedbackRequest(
                "Titel", "Beschreibung", null);

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                feedbackUrl(), request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertValidationMessage(response, "category");
    }

    /** AC: POST mit ungueltigem Kategorie-Wert -> 400 (kein Stacktrace). */
    @Test
    void shouldRejectInvalidCategoryValue() {
        String body = "{\"title\":\"Titel\",\"category\":\"KeineAhnung\"}";

        ResponseEntity<ErrorResponse> response = postJson(body, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse err = response.getBody();
        assertNotNull(err);
        assertNotNull(err.getError(), "Error response must carry a message");
        assertFalse(err.getError().isEmpty());
    }

    /** AC: POST mit zu langem Titel (> 100) -> 400. */
    @Test
    void shouldRejectTitleLongerThan100() {
        String longTitle = "x".repeat(101);
        var request = new CreateFeedbackRequest(
                longTitle, "Beschreibung", FeedbackCategory.Idee);

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                feedbackUrl(), request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertValidationMessage(response, "title");
    }

    /** AC: POST mit zu langer Beschreibung (> 500) -> 400. */
    @Test
    void shouldRejectDescriptionLongerThan500() {
        String longDescription = "y".repeat(501);
        var request = new CreateFeedbackRequest(
                "Titel", longDescription, FeedbackCategory.Sonstiges);

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
                feedbackUrl(), request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertValidationMessage(response, "description");
    }

    /** AC: POST mit leerem/Malformed Body -> 400 statt 500, kein Stacktrace. */
    @Test
    void shouldRejectMalformedJsonBody() {
        String body = "{ this is not json }";

        ResponseEntity<ErrorResponse> response = postJson(body, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse err = response.getBody();
        assertNotNull(err);
        assertNotNull(err.getError(), "Malformed body must yield a message");
        assertFalse(err.getError().isEmpty());
    }

    /** AC: POST mit Null-Body -> 400 statt 500. */
    @Test
    void shouldRejectNullBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = rest.exchange(
                feedbackUrl(), HttpMethod.POST,
                new HttpEntity<>(null, headers), ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- Helfer -------------------------------------------------------------

    private ResponseEntity<ErrorResponse> postJson(String json, Class<ErrorResponse> clazz) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return rest.exchange(feedbackUrl(), HttpMethod.POST, entity, clazz);
    }

    private void assertValidationMessage(ResponseEntity<ErrorResponse> response, String field) {
        ErrorResponse err = response.getBody();
        assertNotNull(err, "Error response must have a body");
        assertNotNull(err.getError(), "Error response must carry a message for '" + field + "'");
        String msg = err.getError();
        assertTrue(msg.toLowerCase().contains(field.toLowerCase()),
                "Validation message should reference the offending field '" + field
                        + "', but was: " + msg);
        assertFalse(msg.contains("Exception"), "Error message must not leak internals/stacktrace: " + msg);
    }
}
