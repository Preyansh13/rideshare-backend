package com.preyansh.ridesharing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Marks this class as a JPA entity stored in the database.
@Entity
// Specifies the table name and prevents duplicate offers for the same ride-driver combination.
@Table(
        name = "ride_offers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ride_offer_ride_driver",
                        columnNames = {"ride_id", "driver_id"}
                )
        }
)
public class RideOffer {

    // Primary key generated automatically by the database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The ride for which this offer was created.
    // One ride can have offers sent to multiple drivers.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    // The driver receiving this particular offer.
    // A driver can receive offers for different rides.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    // Tracks this driver's response to the offer.
    // EnumType.STRING stores readable values in the database.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideOfferStatus status = RideOfferStatus.PENDING;

    // Records when the offer was created.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Required by JPA when loading entities from the database.
    public RideOffer() {
    }

    // Automatically sets the creation time before insertion.
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Returns the offer's database ID.
    public Long getId() {
        return id;
    }

    // Returns the ride associated with this offer.
    public Ride getRide() {
        return ride;
    }

    // Associates this offer with a ride.
    public void setRide(Ride ride) {
        this.ride = ride;
    }

    // Returns the driver receiving this offer.
    public Driver getDriver() {
        return driver;
    }

    // Associates this offer with a driver.
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    // Returns the current offer status.
    public RideOfferStatus getStatus() {
        return status;
    }

    // Updates the offer status.
    public void setStatus(RideOfferStatus status) {
        this.status = status;
    }

    // Returns the offer creation timestamp.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
