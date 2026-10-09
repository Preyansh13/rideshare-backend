package com.preyansh.ridesharing.dto;

// DTO (Data Transfer Object) used to send user information
// from the application to the client in API responses.
public class UserResponse {

    // Unique identifier of the user.
    private Long id;

    // Name of the user.
    private String name;

    // Email address of the user.
    private String email;

    // Phone number of the user.
    private String phone;

    // No-argument constructor, useful for creating an empty response object.
    public UserResponse() {

    }

    // Returns the user's ID.
    public Long getId() {
        return id;
    }

    // Sets the user's ID.
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
}