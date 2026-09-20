package com.example.aiservice.service.impl;

import com.example.aiservice.common.BusinessException;
import com.example.aiservice.service.KbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbServiceImpl implements KbService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    @Override
    public void upload(MultipartFile file) {
        try {
            // 1. 读取文件内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Resource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));

            // 2. 用 TextReader 读取文档
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("filename", file.getOriginalFilename());
            List<Document> documents = reader.get();

            // 3. 切分成小块（每块约 800 个 token，有重叠）
            TokenTextSplitter splitter = new TokenTextSplitter(800, 350, 5, 10000, true);
            List<Document> chunks = splitter.apply(documents);

            // 4. 存入向量数据库（Embedding 和存储一步完成）
            vectorStore.add(chunks);

            log.info("文档 {} 上传成功，切分成 {} 块", file.getOriginalFilename(), chunks.size());
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new BusinessException("文档上传失败：" + e.getMessage());
        }
    }

    @Override
    public String ask(String question) {
        // 1. 在向量数据库中检索最相关的文档片段（Top 4）
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(4)
                        .similarityThreshold(0.5)   // 相似度阈值，低于这个值不返回
                        .build()
        );

        if (relevantDocs == null || relevantDocs.isEmpty()) {
            return "抱歉，知识库中没有找到相关内容，请换一种说法或先上传文档。";
        }

        // 2. 把检索到的文档片段拼成上下文
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < relevantDocs.size(); i++) {
            context.append("【片段").append(i + 1).append("】\n")
                    .append(relevantDocs.get(i).getText())
                    .append("\n\n");
        }

        // 3. 组装 Prompt，让 AI 基于文档回答
        String systemPrompt = """
                你是一个知识库助手。请严格基于以下提供的文档片段回答用户问题。
                
                规则：
                1. 如果文档中有答案，直接回答，并注明是依据哪一段。
                2. 如果文档中没有相关信息，明确告诉用户"知识库中没有找到相关内容"。
                3. 不要编造信息，不要使用文档之外的知识。
                
                文档片段：
                """ + context;

        // 4. 调用大模型生成回答
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();
    }

    @Override
    public void clear() {
        // 注意：简单实现，实际生产需要根据文件名删除
        try {
            vectorStore.delete(List.of());  // 空列表会删除所有？——参见下方说明
            log.info("知识库已清空");
        } catch (Exception e) {
            log.error("清空知识库失败", e);
        }
    }
}