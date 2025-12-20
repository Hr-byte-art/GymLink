package com.ldr.gymlink.ai.memory;

import com.ldr.gymlink.model.entity.ChatMemory;
import com.ldr.gymlink.service.ChatMemoryService;
import dev.langchain4j.community.model.dashscope.QwenTokenCountEstimator;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/12/14 21:33:14
 * @Description 持久化对话记忆
 */
@Component
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {
    @Resource
    private ChatMemoryService chatMemoryService;

    @Resource
    private QwenTokenCountEstimator qwenTokenCountEstimator;
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String messageHistory = chatMemoryService.getMessagesByMemoryId(memoryId);
        if (messageHistory == null || messageHistory.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // 使用 messagesFromJson 反序列化消息列表（与 messagesToJson 对应）
            return ChatMessageDeserializer.messagesFromJson(messageHistory);
        } catch (Exception e) {
            log.error("反序列化聊天记录失败, memoryId: {}", memoryId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> chatMessages) {
        String messages = ChatMessageSerializer.messagesToJson(chatMessages);
        Long userId = Long.valueOf(memoryId.toString());
        
        // 估算 token 数量，如果 API 调用失败则使用简单估算
        int totalTokens = 0;
        try {
            totalTokens = qwenTokenCountEstimator.estimateTokenCountInMessages(chatMessages);
        } catch (Exception e) {
            // Dashscope Tokenization API 可能不稳定，使用简单估算（中文约1.5字符/token）
            log.warn("Token 计数 API 调用失败，使用估算值: {}", e.getMessage());
            totalTokens = estimateTokensSimple(messages);
        }
        
        // 先查询是否存在该用户的记忆记录
        ChatMemory existingMemory = chatMemoryService.getByUserId(userId);
        
        ChatMemory chatMemory;
        if (existingMemory != null) {
            // 存在则更新
            chatMemory = existingMemory;
        } else {
            // 不存在则新建
            chatMemory = new ChatMemory();
            chatMemory.setUserId(userId);
        }
        
        chatMemory.setMessages(messages);
        chatMemory.setMessageCount(chatMessages.size());
        chatMemory.setTotalTokens(totalTokens);

        chatMemoryService.saveOrUpdate(chatMemory);
    }
    
    /**
     * 简单估算 token 数量（中文约1.5字符/token，英文约4字符/token）
     */
    private int estimateTokensSimple(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 简单估算：平均每2个字符约1个token
        return text.length() / 2;
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryService.removeMessageById(memoryId);
    }
}
