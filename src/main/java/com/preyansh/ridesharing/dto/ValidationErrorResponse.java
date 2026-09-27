package com.preyansh.ridesharing.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse {
    private int status;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
    private String path;

    public ValidationErrorResponse(int status, Map<String, String> errors, String path) {
        this.status = status;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}
