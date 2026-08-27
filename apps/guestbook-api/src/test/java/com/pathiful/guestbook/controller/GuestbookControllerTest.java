package com.pathiful.guestbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathiful.guestbook.model.CreateMessageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the guestbook REST API, derived from the issue
 * acceptance criteria:
 * <ul>
 *   <li>GET /api/messages returns all entries (200).</li>
 *   <li>POST /api/messages validates input server-side and stores valid
 *       entries (201 with author/text/id).</li>
 *   <li>Empty / whitespace-only input is rejected (400, understandable
 *       message).</li>
 *   <li>Covers: successful create, retrieve, empty author/text/both, missing
 *       fields, null body.</li>
 * </ul>
 *
 * <p>Tests assert behaviour (status codes, persisted data, clean error
 * bodies), not internal implementation details.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
// Reset the Spring context (and the singleton in-memory repository bean)
// after every test so each test starts with an empty guestbook store.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GuestbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String asJson(CreateMessageRequest request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void getMessagesReturnsEmptyListInitially() throws Exception {
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void postValidMessageCreatesEntryWithAuthorTextAndId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("Alice", "Hallo Welt"))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").value("Alice"))
                .andExpect(jsonPath("$.text").value("Hallo Welt"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // The persisted entry must be retrievable via GET.
        String createdAuthor = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("author").asText();
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].author").value(createdAuthor));
    }

    @Test
    void getReturnsAllStoredEntriesAfterMultiplePosts() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("Alice", "Erster Eintrag"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("Bob", "Zweiter Eintrag"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void postEmptyAuthorRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("", "Hallo"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void postEmptyTextRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("Alice", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postEmptyAuthorAndTextRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void postWhitespaceOnlyAuthorAndTextRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("   ", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void postMissingFieldsRejectedWith400() throws Exception {
        // Missing "author" and "text" keys entirely.
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postNullBodyRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validationErrorDoesNotLeakStacktrace() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new CreateMessageRequest("", "Hallo"))))
                .andExpect(status().isBadRequest())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // Must be a clean JSON error, not a stacktrace or exception text.
        org.assertj.core.api.Assertions.assertThat(body)
                .as("error body must not leak a stacktrace")
                .doesNotContain("Exception", "at com.", "Caused by");
    }
}
