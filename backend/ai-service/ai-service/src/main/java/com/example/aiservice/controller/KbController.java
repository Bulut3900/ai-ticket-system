package com.example.aiservice.controller;

import com.example.aiservice.common.Result;
import com.example.aiservice.dto.KbQueryRequest;
import com.example.aiservice.service.KbService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    /**
     * 上传文档到知识库
     */
    @PostMapping("/upload")
    public Result<Void> upload(@RequestParam("file") MultipartFile file) {
        kbService.upload(file);
        return Result.success();
    }

    /**
     * 基于知识库提问
     */
    @PostMapping("/ask")
    public Result<String> ask(@Valid @RequestBody KbQueryRequest request) {
        String answer = kbService.ask(request.getQuestion());
        return Result.success(answer);
    }

    /**
     * 清空知识库（管理员用）
     */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        kbService.clear();
        return Result.success();
    }
}