package com.pathiful.guestbook.repository;

import com.pathiful.guestbook.model.GuestbookEntry;

import java.util.List;

/**
 * In-memory store for guestbook messages.
 */
public interface GuestbookRepository {

    GuestbookEntry save(GuestbookEntry entry);

    List<GuestbookEntry> findAll();
}
