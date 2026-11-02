package com.preyansh.ridesharing.exception;

public class VehicleAlreadyAssignedException extends RuntimeException {
    public VehicleAlreadyAssignedException(String message) {
        super(message);
    }
}
