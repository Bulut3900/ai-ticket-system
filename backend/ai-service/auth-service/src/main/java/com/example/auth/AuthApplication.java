package com.example.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// 新增 scanBasePackages，扫描 com.example 下所有模块
@SpringBootApplication(scanBasePackages = "com.example")
@EnableDiscoveryClient
@MapperScan("com.example.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
