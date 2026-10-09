package com.preyansh.ridesharing.model;

import jakarta.persistence.*;

// Marks this class as a JPA entity that maps to a database table.
@Entity

// Specifies the database table name for this entity.
@Table(name = "users")
public class User {

    // Marks this field as the primary key of the users table.
    @Id

    // Lets the database generate the ID when a new user is inserted.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stores the user's name.
    private String name;

    // Stores the user's email address.
    // unique = true enforces uniqueness at the database level.
    @Column(unique = true)
    private String email;

    // Stores the user's phone number.
    private String phone;

    // Stores the user's role as a string, such as "RIDER" or "DRIVER",
    // instead of storing the enum's numeric ordinal.
    @Enumerated(EnumType.STRING)
    private Role role;

    // No-argument constructor required by JPA when loading entities.
    public User() {

    }

    // Returns the user's primary key.
    public Long getId() {
        return id;
    }

    // Sets the user's primary key.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the user's name.
    public String getName() {
        return name;
    }

    // Sets the user's name.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the user's email address.
    public String getEmail() {
        return email;
    }

    // Sets the user's email address.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the user's phone number.
    public String getPhone() {
        return phone;
    }

    // Sets the user's phone number.
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Returns the user's role.
    public Role getRole() {
        return role;
    }

    // Sets the user's role.
    public void setRole(Role role) {
        this.role = role;
    }
}