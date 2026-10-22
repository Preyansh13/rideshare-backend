package com.preyansh.ridesharing.exception;

// Represents a conflict caused by an already registered vehicle.
public class VehicleAlreadyExistsException extends RuntimeException {
    // Passes the conflict message to the parent exception.
    public VehicleAlreadyExistsException(String message) {
        super(message);
    }
}
