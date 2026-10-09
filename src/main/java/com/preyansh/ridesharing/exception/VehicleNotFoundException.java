package com.preyansh.ridesharing.exception;

// Custom unchecked exception thrown when a requested vehicle
// cannot be found in the application or database.
public class VehicleNotFoundException extends RuntimeException {

    // Accepts an error message and passes it to the parent RuntimeException class.
    public VehicleNotFoundException(String message) {
        super(message);
    }
}