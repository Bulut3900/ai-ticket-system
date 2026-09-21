package com.example.ticket.feign;

import com.example.ticket.dto.TicketAnalysis;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign 客户端：调用 ai-service 的分析接口
 */
@FeignClient(name = "ai-service")   // ← 从 Nacos 找服务名为 ai-service 的服务
public interface AiServiceClient {

    /**
     * 调用 ai-service 的 /internal/ai/analyze 接口
     */
    @GetMapping("/internal/ai/analyze")
    TicketAnalysis analyze(@RequestParam("message") String message);
}