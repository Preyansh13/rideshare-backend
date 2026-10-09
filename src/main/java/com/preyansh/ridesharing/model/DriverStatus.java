package com.preyansh.ridesharing.model;

// Enum defining the possible states of a driver in the ride-sharing system.
public enum DriverStatus {

    // Driver is online and available to accept a ride.
    AVAILABLE,

    // Driver is currently occupied, for example, completing a ride.
    BUSY,

    // Driver is offline and not accepting rides.
    OFFLINE
}