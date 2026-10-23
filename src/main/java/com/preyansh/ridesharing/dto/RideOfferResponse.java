package com.preyansh.ridesharing.dto;

import com.preyansh.ridesharing.model.RideOfferStatus;
import java.time.LocalDateTime;

// Represents the ride-offer information returned by the API.
public record RideOfferResponse(
        Long id,
        Long rideId,
        Long driverId,
        RideOfferStatus status,
        LocalDateTime createdAt
) {
}
