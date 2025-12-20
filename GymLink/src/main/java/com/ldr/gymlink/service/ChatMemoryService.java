package com.ldr.gymlink.service;

import com.ldr.gymlink.model.entity.ChatMemory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 木子宸
* @description 针对表【chat_memory(AI助手对话记忆表)】的数据库操作Service
* @createDate 2025-12-14 22:26:21
*/
public interface ChatMemoryService extends IService<ChatMemory> {

    /**
     * 根据memoryId获取消息
     * @param memoryId memoryId
     * @return 消息
     */
    String getMessagesByMemoryId(Object memoryId);

    /**
     * 根据memoryId删除消息
     * @param memoryId memoryId
     */
    void removeMessageById(Object memoryId);

    /**
     * 根据userId获取ChatMemory实体
     * @param userId 用户ID
     * @return ChatMemory实体
     */
    ChatMemory getByUserId(Long userId);
}
