package com.preyansh.ridesharing.exception;

// Thrown when attempting to delete a vehicle assigned to a driver.
public class VehicleInUseException extends RuntimeException {
    // Passes the error message to the parent exception.
    public VehicleInUseException(String message) {
        super(message);
    }
}
