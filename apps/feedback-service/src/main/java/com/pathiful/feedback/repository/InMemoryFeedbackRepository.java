package com.pathiful.feedback.repository;

import com.pathiful.feedback.model.FeedbackCategory;
import com.pathiful.feedback.model.FeedbackEntry;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Minimale Persistenz: In-Memory-Speicher auf Basis einer ConcurrentHashMap,
 * analog zu den bestehenden Standalone-Services (guestbook-api, todo-service).
 * Eine eigene Datenbank ist für das Feedback-Board laut Issue #55 nicht nötig.
 */
@Repository
public class InMemoryFeedbackRepository {

    private final ConcurrentHashMap<Long, FeedbackEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public FeedbackEntry save(String title, String description, FeedbackCategory category) {
        FeedbackEntry entry = new FeedbackEntry(
                idGen.getAndIncrement(),
                title,
                description,
                category,
                Instant.now()
        );
        store.put(entry.getId(), entry);
        return entry;
    }

    public List<FeedbackEntry> findAll() {
        List<FeedbackEntry> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(FeedbackEntry::getId));
        return list;
    }
}
