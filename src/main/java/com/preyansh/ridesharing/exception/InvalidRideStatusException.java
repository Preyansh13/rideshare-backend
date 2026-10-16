package com.preyansh.ridesharing.exception;

public class InvalidRideStatusException extends RuntimeException {
    public InvalidRideStatusException(String message) {
        super(message);
    }
}
