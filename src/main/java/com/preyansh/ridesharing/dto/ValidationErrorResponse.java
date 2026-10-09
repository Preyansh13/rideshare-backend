package com.preyansh.ridesharing.dto;

import java.time.LocalDateTime;
import java.util.Map;

// DTO used to return validation errors in a structured API response.
public class ValidationErrorResponse {

    // HTTP status code associated with the validation failure, typically 400.
    private int status;

    // Stores field names as keys and their corresponding validation messages as values.
    private Map<String, String> errors;

    // Date and time when this error response object was created.
    private LocalDateTime timestamp;

    // Request URI path where the validation error occurred.
    private String path;

    // Initializes the validation error details and records the current timestamp.
    public ValidationErrorResponse(int status, Map<String, String> errors, String path) {
        this.status = status;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    // Returns the HTTP status code.
    public int getStatus() {
        return status;
    }

    // Returns the map containing field-specific validation errors.
    public Map<String, String> getErrors() {
        return errors;
    }

    // Returns the timestamp of the error response.
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Returns the request path associated with the validation error.
    public String getPath() {
        return path;
    }
}