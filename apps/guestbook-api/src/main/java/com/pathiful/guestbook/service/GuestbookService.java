package com.pathiful.guestbook.service;

import com.pathiful.guestbook.model.GuestbookEntry;
import com.pathiful.guestbook.repository.GuestbookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for guestbook operations.
 */
@Service
public class GuestbookService {

    private final GuestbookRepository repository;

    public GuestbookService(GuestbookRepository repository) {
        this.repository = repository;
    }

    public GuestbookEntry create(String author, String text) {
        GuestbookEntry entry = new GuestbookEntry(null, author, text);
        return repository.save(entry);
    }

    public List<GuestbookEntry> listAll() {
        return repository.findAll();
    }
}
