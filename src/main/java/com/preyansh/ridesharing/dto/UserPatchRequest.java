package com.preyansh.ridesharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

// DTO for receiving fields in a partial user update.
public class UserPatchRequest {
    // Updated name; must not be blank if provided.
    @Pattern(regexp = ".*\\S.*", message = "Name must not be blank")
    private String name;

    // Updated email; must be valid if provided.
    @Email(message = "Email must be valid")
    @Pattern(regexp = ".*\\S.*", message = "Email must not be blank")
    private String email;

    // Updated phone; must contain exactly 10 digits if provided.
    @Pattern(regexp = "[0-9]{10}",
            message = "Phone number must contain exactly 10 digits")
    private String phone;

    // No-argument constructor for JSON deserialization.
    public UserPatchRequest() {
    }

    // Returns the supplied name.
    public String getName() {
        return name;
    }

    // Sets the name for the partial update.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the supplied email.
    public String getEmail() {
        return email;
    }

    // Sets the email for the partial update.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the supplied phone number.
    public String getPhone() {
        return phone;
    }

    // Sets the phone number for the partial update.
    public void setPhone(String phone) {
        this.phone = phone;
    }
}