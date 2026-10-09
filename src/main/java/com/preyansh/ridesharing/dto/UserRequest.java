package com.preyansh.ridesharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// DTO used to receive and validate user data when creating or updating a user.
public class UserRequest {

    // Rejects null, empty, or whitespace-only names.
    // Returns the specified message when validation fails.
    @NotBlank(message = "Name is required")
    private String name;

    // Ensures the email is provided and follows a valid email format.
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Ensures the phone number is provided and contains exactly 10 digits.
    // The regular expression allows only digits from 0 to 9.
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must contain exactly 10 digits")
    private String phone;

    // No-argument constructor, useful for mapping incoming JSON to this DTO.
    public UserRequest() {

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