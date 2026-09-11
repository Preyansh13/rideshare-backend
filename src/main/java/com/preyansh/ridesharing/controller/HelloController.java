package com.preyansh.ridesharing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Ride Sharing Backend is running!";
    }

//    For practice purpose of @PathVariable and @RequestParam annotations

//    @GetMapping("/hello/{name}")
//    public String helloName(@PathVariable String name) {
//        return "Hello " + name + "! This is @PathVariable trial.";
//    }
//
//    @GetMapping("/greet")
//    public String greet(@RequestParam String name) {
//        return "Hello " + name + "! This is @RequestParam trial.";
//    }

}