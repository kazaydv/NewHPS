package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class Hellocontroller {

    private List<User> users = new ArrayList<>();

    @GetMapping
    public List<User> getUsers() {
        return users;
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        users.add(user);
        return user;
    }
}

class User {
    public String name;
    public int age;

    public User() {}  // Important for POST

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
