package com.example.aiservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface KbService {

    /**
     * 上传文档到知识库
     */
    void upload(MultipartFile file);

    /**
     * 基于知识库回答用户问题
     */
    String ask(String question);

    /**
     * 清空知识库
     */
    void clear();
}