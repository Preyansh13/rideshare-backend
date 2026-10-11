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

    // The user who requested this ride.
    // A user can request multiple rides.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    // The driver assigned to this ride.
    // This can remain null until a driver accepts the ride.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    // Pickup location is mandatory for every ride.
    @Column(nullable = false)
    private String pickupLocation;

    // Destination location is mandatory for every ride.
    @Column(nullable = false)
    private String dropoffLocation;

    // Records when the ride was requested.
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // Stores the current lifecycle state of this ride.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    // No-argument constructor required by JPA.
    public Ride() {

    }

    // Runs before a new Ride entity is inserted into the database.
    @PrePersist
    protected void onCreate() {
        // Set the request time automatically if it was not provided.
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    // Returns the database-generated ride ID.
    public Long getId() {
        return id;
    }

    // Returns the user who requested this ride.
    public User getRider() {
        return rider;
    }

    // Assigns the rider to this ride.
    public void setRider(User rider) {
        this.rider = rider;
    }

    // Returns the driver assigned to this ride, if any.
    public Driver getDriver() {
        return driver;
    }

    // Assigns a driver to this ride.
    public void setDriver(Driver driver) {
        this.driver = driver;
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
