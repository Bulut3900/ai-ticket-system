package com.example.aiservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiservice.common.BusinessException;
import com.example.aiservice.dto.LoginRequest;
import com.example.aiservice.dto.LoginResponse;
import com.example.aiservice.dto.RegisterRequest;
import com.example.aiservice.entity.User;
import com.example.aiservice.mapper.UserMapper;
import com.example.aiservice.service.UserService;
import com.example.aiservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");

        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 根据用户名查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 4. 返回
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }
}