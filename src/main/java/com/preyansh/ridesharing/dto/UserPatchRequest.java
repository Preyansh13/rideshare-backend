package com.preyansh.ridesharing.dto;

// DTO (Data Transfer Object) used to receive fields for a partial user update.
// Clients can provide only the fields they want to change.
public class UserPatchRequest {

    // Updated name, if provided in the request.
    private String name;

    // Updated email, if provided in the request.
    private String email;

    // Updated phone number, if provided in the request.
    private String phone;

    // No-argument constructor, useful for mapping incoming JSON to this object.
    public UserPatchRequest() {

    }

    // Returns the name supplied for the partial update.
    public String getName() {
        return name;
    }

    // Sets the name supplied for the partial update.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the email supplied for the partial update.
    public String getEmail() {
        return email;
    }

    // Sets the email supplied for the partial update.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the phone number supplied for the partial update.
    public String getPhone() {
        return phone;
    }

    // Sets the phone number supplied for the partial update.
    public void setPhone(String phone) {
        this.phone = phone;
    }
}