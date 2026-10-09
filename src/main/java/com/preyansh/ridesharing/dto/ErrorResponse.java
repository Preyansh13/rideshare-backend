package com.preyansh.ridesharing.dto;

import java.time.LocalDateTime;

// DTO used to provide a consistent structure for API error responses.
public class ErrorResponse {

    // HTTP status code associated with the error, such as 400, 404, or 409.
    private int status;

    // Human-readable description of what went wrong.
    private String message;

    // Date and time when this error response object was created.
    private LocalDateTime timestamp;

    // Request URI path where the error occurred.
    private String path;

    // Initializes the error details and automatically records the current timestamp.
    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    // Returns the HTTP status code.
    public int getStatus() {
        return status;
    }

    // Returns the error message.
    public String getMessage() {
        return message;
    }

    // Returns the timestamp of the error response.
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Returns the request path associated with the error.
    public String getPath() {
        return path;
    }
}