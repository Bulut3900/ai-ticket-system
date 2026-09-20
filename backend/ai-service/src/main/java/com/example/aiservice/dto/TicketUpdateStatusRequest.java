package com.example.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketUpdateStatusRequest {

    @NotBlank(message = "状态不能为空")
    private String status;    // PENDING / PROCESSING / RESOLVED / CLOSED
}