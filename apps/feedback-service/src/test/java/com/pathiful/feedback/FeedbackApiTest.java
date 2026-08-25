package com.pathiful.feedback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathiful.feedback.model.FeedbackCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstests für die Feedback-Board REST API (Issue #55).
 * Abgeleitet aus den Akzeptanzkriterien; Tests prüfen Verhalten, nicht Implementierung.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeedbackApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    private String feedbackUrl;

    @BeforeEach
    void setUp() {
        feedbackUrl = "http://localhost:" + port + "/feedback";
    }

    /** AC: POST /feedback mit gültigen Daten -> 201 + Eintrag mit allen Feldern. */
    @Test
    void shouldCreateFeedbackAndReturn201() throws Exception {
        ResponseEntity<String> response = postJson(
                "{\"title\":\"Fehler auf Mobil\",\"description\":\"Layout bricht um\",\"category\":\"Bug\"}");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.get("id").asLong() > 0, "Eintrag muss eine id haben");
        assertEquals("Fehler auf Mobil", body.get("title").asText());
        assertEquals("Layout bricht um", body.get("description").asText());
        assertEquals("Bug", body.get("category").asText());
        assertFalse(body.get("createdAt").asText().isEmpty(), "createdAt darf nicht leer sein");
    }

    /** AC: Die Beschreibung ist optional – ein Eintrag ohne description ist gültig. */
    @Test
    void shouldCreateFeedbackWithoutDescription() {
        ResponseEntity<String> response = postJson(
                "{\"title\":\"Idee\",\"category\":\"Idee\"}");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    /** AC: GET /feedback -> 200 + Liste der zuvor erstellten Einträge. */
    @Test
    void shouldListAllFeedback() throws Exception {
        postJson("{\"title\":\"Eins\",\"category\":\"Idee\"}");
        postJson("{\"title\":\"Zwei\",\"category\":\"Sonstiges\"}");

        ResponseEntity<String> response = rest.getForEntity(feedbackUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode list = objectMapper.readTree(response.getBody());
        assertTrue(list.isArray() && list.size() >= 2, "Liste muss mindestens zwei Einträge enthalten");
        boolean hasEins = false;
        boolean hasZwei = false;
        for (JsonNode n : list) {
            if ("Eins".equals(n.get("title").asText())) hasEins = true;
            if ("Zwei".equals(n.get("title").asText())) hasZwei = true;
        }
        assertTrue(hasEins, "Liste muss 'Eins' enthalten");
        assertTrue(hasZwei, "Liste muss 'Zwei' enthalten");
    }

    /** AC: Ungültige Eingaben werden serverseitig abgewiesen (4xx). */
    @Test
    void shouldRejectBlankTitle() throws Exception {
        ResponseEntity<String> response = postJson(
                "{\"title\":\"   \",\"category\":\"Bug\"}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertNotNull(body.get("error"), "Fehlermeldung muss im error-Feld stehen");
        assertFalse(body.get("error").asText().isEmpty());
    }

    /** Ungültige Kategorie -> 400 (kein Stacktrace an den Client). */
    @Test
    void shouldRejectInvalidCategory() throws Exception {
        ResponseEntity<String> response = postJson(
                "{\"title\":\"Titel\",\"category\":\"Unbekannt\"}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertNotNull(body.get("error"));
    }

    /** Fehlende Kategorie -> 400. */
    @Test
    void shouldRejectMissingCategory() throws Exception {
        ResponseEntity<String> response = postJson(
                "{\"title\":\"Titel\"}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** Fehlende Felder (leerer JSON-Body) -> 400. */
    @Test
    void shouldRejectMissingFields() throws Exception {
        ResponseEntity<String> response = postJson("{}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** Null-Body -> 400. */
    @Test
    void shouldRejectNullBody() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = rest.exchange(
                feedbackUrl, HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** Titel länger als 100 Zeichen -> 400. */
    @Test
    void shouldRejectTitleTooLong() throws Exception {
        String longTitle = "x".repeat(101);
        ResponseEntity<String> response = postJson(
                "{\"title\":\"" + longTitle + "\",\"category\":\"Bug\"}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- Helfer ---

    private ResponseEntity<String> postJson(String bodyJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        return rest.exchange(feedbackUrl, HttpMethod.POST, entity, String.class);
    }

    /** Bequemer Zugriff – hält die Referenz auf FeedbackEntry (nicht benötigt, Doku-Zweck). */
    @SuppressWarnings("unused")
    private Map<String, Object> sampleBody() {
        return Map.of(
                "title", "Beispiel",
                "description", "Beschreibung",
                "category", FeedbackCategory.Idee.name()
        );
    }
}
