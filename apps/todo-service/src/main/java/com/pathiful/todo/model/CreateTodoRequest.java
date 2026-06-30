package com.pathiful.todo.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a To-Do.
 */
public class CreateTodoRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    public CreateTodoRequest() {
    }

    public CreateTodoRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
