package com.pathiful.guestbook.model;

import java.util.concurrent.atomic.AtomicLong;

public class GuestbookEntry {

    private static final AtomicLong ID_GEN = new AtomicLong(1);

    private Long id;
    private String author;
    private String text;

    public GuestbookEntry() {
    }

    public GuestbookEntry(String author, String text) {
        this.id = ID_GEN.getAndIncrement();
        this.author = author;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
