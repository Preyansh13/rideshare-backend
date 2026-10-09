package com.preyansh.ridesharing.exception;

// Custom unchecked exception used when attempting to create
// a user who already exists, such as a user with a duplicate email.
public class UserAlreadyExistsException extends RuntimeException {

    // Accepts an error message and passes it to the parent RuntimeException class.
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}