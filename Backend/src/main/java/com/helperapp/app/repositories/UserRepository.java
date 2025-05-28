package com.helperapp.app.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.User;


public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUsernameOrEmail(String username, String email);
}