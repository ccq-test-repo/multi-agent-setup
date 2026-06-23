package com.pathiful.common;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " mit ID " + id + " nicht gefunden.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
