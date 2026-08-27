package com.pathiful.guestbook.repository;

import com.pathiful.guestbook.model.GuestbookEntry;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory guestbook store backed by a ConcurrentHashMap and an
 * AtomicLong id sequence. Deliberately no database.
 */
@Repository
public class InMemoryGuestbookRepository implements GuestbookRepository {

    private final ConcurrentHashMap<Long, GuestbookEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Override
    public GuestbookEntry save(GuestbookEntry entry) {
        if (entry.getId() == null) {
            entry.setId(idSeq.getAndIncrement());
        }
        store.put(entry.getId(), entry);
        return entry;
    }

    @Override
    public List<GuestbookEntry> findAll() {
        List<GuestbookEntry> entries = new ArrayList<>(store.values());
        entries.sort(Comparator.comparing(GuestbookEntry::getId));
        return entries;
    }
}
