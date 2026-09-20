package com.example.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KbQueryRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}