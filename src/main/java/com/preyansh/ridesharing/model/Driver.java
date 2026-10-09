package com.preyansh.ridesharing.model;

import jakarta.persistence.*;

// Marks this class as a JPA entity that maps to a database table.
@Entity

// Specifies the database table name for this entity.
@Table(name = "drivers")
public class Driver {

    // Marks this field as the primary key of the drivers table.
    @Id

    // Lets the database generate the ID when a new driver is inserted.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Defines a one-to-one relationship between a driver and a user.
    // The user_id column stores the associated user's foreign key.
    // unique = true ensures that a user can be associated with at most one driver.
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Defines a one-to-one relationship between a driver and a vehicle.
    // The vehicle_id column stores the associated vehicle's foreign key.
    // unique = true ensures that a vehicle can be associated with at most one driver.
    @OneToOne
    @JoinColumn(name = "vehicle_id", unique = true)
    private Vehicle vehicle;

    // Driving license number associated with the driver.
    private String licenseNumber;

    // Stores the driver's status as a string in the database
    // rather than as the enum's numeric ordinal.
    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    // No-argument constructor required by JPA when loading entities.
    public Driver() {

    }

    // Returns the driver's primary key.
    public Long getId() {
        return id;
    }

    // Sets the driver's primary key.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the User entity associated with this driver.
    public User getUser() {
        return user;
    }

    // Associates a User entity with this driver.
    public void setUser(User user) {
        this.user = user;
    }

    // Returns the Vehicle entity associated with this driver.
    public Vehicle getVehicle() {
        return vehicle;
    }

    // Associates a Vehicle entity with this driver.
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    // Returns the driver's license number.
    public String getLicenseNumber() {
        return licenseNumber;
    }

    // Sets the driver's license number.
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    // Returns the driver's current status.
    public DriverStatus getStatus() {
        return status;
    }

    // Sets the driver's current status.
    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}