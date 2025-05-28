package com.helperapp.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helperapp.app.models.User;
import com.helperapp.app.security.JwtHelper;
import com.helperapp.app.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {

        System.out.println("User ID from token: " + jwtHelper.getUserIdFromToken());
        System.out.println("User role from token: " + jwtHelper.getRoleFromToken());
        if (!jwtHelper.getRoleFromToken().toLowerCase().equals("admin")) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {

        if (!jwtHelper.getUserIdFromToken().equals(id) && !jwtHelper.getRoleFromToken().equals("admin")) {
            return ResponseEntity.status(403).build();
        }

        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {

        if (!jwtHelper.getUserIdFromToken().equals(id) && !jwtHelper.getRoleFromToken().equals("admin")) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {

        if (!jwtHelper.getUserIdFromToken().equals(id) && !jwtHelper.getRoleFromToken().equals("admin")) {
            return ResponseEntity.status(403).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
