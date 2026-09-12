package com.preyansh.ridesharing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.preyansh.ridesharing.model.User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public User getUser() {
        User user = new User();

        user.setId(1L);
        user.setName("Preyansh");
        user.setEmail("preyansh@example.com");
        user.setPhone("9876543210");

        return user;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        User user = new User();

        user.setId(id);
        user.setName("Preyansh");
        user.setEmail("preyansh@example.com");
        user.setPhone("9876543210");

        return user;
    }
}
