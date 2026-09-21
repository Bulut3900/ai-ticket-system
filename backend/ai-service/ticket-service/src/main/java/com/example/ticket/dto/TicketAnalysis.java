package com.example.ticket.dto;

public record TicketAnalysis(
        String summary,     // 摘要
        String category,    // 类别
        String priority     // 优先级
) {
}