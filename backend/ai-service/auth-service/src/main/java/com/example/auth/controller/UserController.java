package com.example.auth.controller;

import com.example.common.Result;                                 // ← common
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 类体保持不变
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }
}