package com.preyansh.ridesharing.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import com.preyansh.ridesharing.model.User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private List<User> users = new ArrayList<>();

    public UserController() {

        User user1 = new User();
        user1.setId(1L);
        user1.setName("Preyansh");
        user1.setEmail("preyansh@example.com");
        user1.setPhone("9876543210");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Manas");
        user2.setEmail("manas@example.com");
        user2.setPhone("9876543211");

        User user3 = new User();
        user3.setId(3L);
        user3.setName("Saaransh");
        user3.setEmail("saaransh@example.com");
        user3.setPhone("9876543212");

        users.add(user1);
        users.add(user2);
        users.add(user3);

    }

    @GetMapping
    public List<User> getUsers() {

        return users;

    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {

        for (User user : users) {
            if (user.getId().equals(id)) {
                return ResponseEntity.ok(user);
            }
        }
//        User user = new User();
//
//        user.setId(id);
//        user.setName("Preyansh");
//        user.setEmail("preyansh@example.com");
//        user.setPhone("9876543210");

        return ResponseEntity.notFound().build();
    }
}
