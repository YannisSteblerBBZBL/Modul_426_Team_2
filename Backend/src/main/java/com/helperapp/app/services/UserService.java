package com.helperapp.app.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.User;
import com.helperapp.app.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {

        return userRepository.findById(id);
    }

    public User createUser(User user) {

        System.out.println(user);

        user.setAge(String.valueOf(LocalDateTime.now().getYear() - LocalDate.parse(user.getBirthday()).getYear()));

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User updateUser(String id, User userDetails) {
        userDetails.setId(id);

        userDetails.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(userDetails);
    }

    public void deleteUser(String id) {
        Optional<User> user = userRepository.findById(id);
        userRepository.save(user.get());
    }
}
