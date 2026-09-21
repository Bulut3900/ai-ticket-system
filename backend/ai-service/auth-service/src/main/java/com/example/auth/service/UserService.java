package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;

public interface UserService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}