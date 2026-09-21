package com.example.aiservice.service;

import com.example.aiservice.dto.LoginRequest;
import com.example.aiservice.dto.LoginResponse;
import com.example.aiservice.dto.RegisterRequest;

public interface UserService {

    /**
     * 注册
     */
    void register(RegisterRequest request);

    /**
     * 登录
     */
    LoginResponse login(LoginRequest request);
}