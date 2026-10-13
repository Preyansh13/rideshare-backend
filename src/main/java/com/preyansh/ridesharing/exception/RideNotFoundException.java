package com.preyansh.ridesharing.exception;

// Custom exception thrown when a requested ride cannot be found.
public class RideNotFoundException extends RuntimeException {

    // Passes the error message to the RuntimeException parent class.
    public RideNotFoundException(String message) {
        super(message);
    }
}
