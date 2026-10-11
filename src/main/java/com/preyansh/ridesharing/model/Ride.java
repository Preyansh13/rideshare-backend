package com.preyansh.ridesharing.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Marks this class as a JPA entity that will be stored in the database.
@Entity
// Specifies the database table name for this entity.
@Table(name = "rides")
public class Ride {

    // Primary key generated automatically by PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pickup location is mandatory for every ride.
    @Column(nullable = false)
    private String pickupLocation;

    // Destination location is mandatory for every ride.
    @Column(nullable = false)
    private String dropoffLocation;

    // Records when the ride was requested.
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // No-argument constructor required by JPA.
    public Ride() {

    }

    // Returns the database-generated ride ID.
    public Long getId() {
        return id;
    }

    // Returns the pickup location.
    public String getPickupLocation() {
        return pickupLocation;
    }

    // Updates the pickup location.
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    // Returns the destination location.
    public String getDropoffLocation() {
        return dropoffLocation;
    }

    // Updates the destination location.
    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    // Returns the ride request timestamp.
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    // Sets the ride request timestamp.
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
