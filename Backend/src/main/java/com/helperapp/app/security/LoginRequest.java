package com.helperapp.app.security;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier;
    private String password;
}