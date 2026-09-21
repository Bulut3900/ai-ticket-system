package com.example.aiservice.controller;

import com.example.aiservice.model.TicketAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供 ticket-service 通过 Feign 调用的内部接口
 */
@RestController
@RequestMapping("/internal/ai")
public class AiAnalyzeController {

    private final ChatClient.Builder chatClientBuilder;

    public AiAnalyzeController(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    /**
     * 工单智能分析
     * 路径：GET /internal/ai/analyze?message=xxx
     */
    @GetMapping("/analyze")
    public TicketAnalysis analyze(@RequestParam("message") String message) {
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .system("你是一个工单分析助手。请分析用户的问题，提取摘要、类别和优先级。" +
                        "类别只能是：技术故障、业务咨询、投诉建议、其他。" +
                        "优先级只能是：高、中、低。")
                .user(message)
                .call()
                .entity(TicketAnalysis.class);
    }
}