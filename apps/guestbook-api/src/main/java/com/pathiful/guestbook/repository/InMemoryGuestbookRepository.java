package com.pathiful.guestbook.repository;

import com.pathiful.guestbook.model.GuestbookEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@org.springframework.stereotype.Repository
public class InMemoryGuestbookRepository {

    private final ConcurrentHashMap<Long, GuestbookEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public GuestbookEntry save(String author, String text) {
        GuestbookEntry entry = new GuestbookEntry(author, text);
        store.put(entry.getId(), entry);
        return entry;
    }

    public List<GuestbookEntry> findAll() {
        List<GuestbookEntry> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(GuestbookEntry::getId));
        return list;
    }
}
