package com.helperapp.app.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    // General Information
    private String firstname;
    private String lastname;
    private String username;
    private String email;

    // Additional Information
    private String birthday;
    private String age;

    // Authentication Information
    private String passwordHash;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
