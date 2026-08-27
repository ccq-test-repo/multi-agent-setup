package com.pathiful.guestbook.model;

/**
 * Guestbook entry entity with id, author, and text.
 */
public class GuestbookEntry {

    private Long id;
    private String author;
    private String text;

    public GuestbookEntry() {
    }

    public GuestbookEntry(Long id, String author, String text) {
        this.id = id;
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
