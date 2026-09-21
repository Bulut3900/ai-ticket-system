package com.example.aiservice.controller;
import com.example.aiservice.model.TicketAnalysis;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        // 1. 创建内存仓库
        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        // 2. 创建消息窗口记忆，保留最近 10 条消息
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(10)
                .build();

        // 3. 构建带记忆的 ChatClient
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * 普通对话（带记忆）
     */
    @GetMapping("/ai/chat")
    public String chat(
            @RequestParam(value = "message", defaultValue = "你好") String message,
            @RequestParam(value = "sessionId", defaultValue = "default") String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", sessionId))
                .call()
                .content();
    }

    /**
     * 流式对话（带记忆）
     */
    @GetMapping(value = "/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @RequestParam(value = "message", defaultValue = "你好") String message,
            @RequestParam(value = "sessionId", defaultValue = "default") String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", sessionId))
                .stream()
                .content();
    }

    /**
     * 结构化输出：智能工单分析
     * 返回 JSON 格式：{ "summary": "...", "category": "...", "priority": "..." }
     */
    @GetMapping("/ai/analyze")
    public TicketAnalysis analyze(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("你是一个工单分析助手。请分析用户的问题，提取摘要、类别和优先级。" +
                        "类别只能是：技术故障、业务咨询、投诉建议、其他。" +
                        "优先级只能是：高、中、低。")
                .user(message)
                .call()
                .entity(TicketAnalysis.class);
    }
}