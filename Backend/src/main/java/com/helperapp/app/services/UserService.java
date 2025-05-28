package com.helperapp.app.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.User;
import com.helperapp.app.repositories.UserRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtHelper jwtHelper;

    public List<User> getAllUsers() {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return userRepository.findAll().stream()
                .filter(user -> user.getId().equals(currentUserId))
                .toList();
    }

    public Optional<User> getUserById(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        if (!id.equals(currentUserId)) {
            return Optional.empty(); // or throw new SecurityException("Access denied")
        }
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        // Optional: you might not use JWT for sign-up, skip auth check if needed
        user.setAge(String.valueOf(LocalDateTime.now().getYear() - LocalDate.parse(user.getBirthday()).getYear()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUser(String id, User userDetails) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        if (!id.equals(currentUserId)) {
            throw new SecurityException("Access denied: cannot update other users.");
        }

        userDetails.setId(id);
        userDetails.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(userDetails);
    }

    public void deleteUser(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        if (!id.equals(currentUserId)) {
            throw new SecurityException("Access denied: cannot delete other users.");
        }

        userRepository.deleteById(id);
    }
}
