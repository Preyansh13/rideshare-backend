package com.preyansh.ridesharing.exception;

// Custom unchecked exception thrown when a requested user
// cannot be found in the application or database.
public class UserNotFoundException extends RuntimeException {

    // Accepts an error message and passes it to the parent RuntimeException class.
    public UserNotFoundException(String message) {
        super(message);
    }
}