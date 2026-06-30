package com.pathiful.guestbook.model;

import jakarta.validation.constraints.NotBlank;

public class CreateMessageRequest {

    @NotBlank(message = "author must not be blank")
    private String author;

    @NotBlank(message = "text must not be blank")
    private String text;

    public CreateMessageRequest() {
    }

    public CreateMessageRequest(String author, String text) {
        this.author = author;
        this.text = text;
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
