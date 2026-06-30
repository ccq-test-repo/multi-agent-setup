package com.pathiful.todo.model;

/**
 * To-Do entity with id, title, and done flag.
 */
public class Todo {

    private long id;
    private String title;
    private boolean done;

    public Todo() {
    }

    public Todo(String title) {
        this.title = title;
        this.done = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
