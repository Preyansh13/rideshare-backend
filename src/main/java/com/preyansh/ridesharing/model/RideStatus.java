package com.preyansh.ridesharing.model;

// Defines the valid lifecycle states of a ride.
public enum RideStatus {

    REQUESTED,   // A rider has requested a ride.
    ACCEPTED,    // A driver has accepted the request.
    IN_PROGRESS, // The rider's trip has started.
    COMPLETED,   // The trip has finished successfully.
    CANCELLED,   // The ride was cancelled.
    REJECTED     // The ride request was rejected.
}
