package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.model.entity.ChatMemory;
import com.ldr.gymlink.service.ChatMemoryService;
import com.ldr.gymlink.mapper.ChatMemoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 木子宸
* @description 针对表【chat_memory(AI助手对话记忆表)】的数据库操作Service实现
* @createDate 2025-12-14 22:26:21
*/
@Service
public class ChatMemoryServiceImpl extends ServiceImpl<ChatMemoryMapper, ChatMemory>
    implements ChatMemoryService{

    @Override
    public String getMessagesByMemoryId(Object memoryId) {
        Long userId = Long.valueOf(memoryId.toString());
        LambdaQueryWrapper<ChatMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMemory::getUserId, userId)
                .orderByDesc(ChatMemory::getUpdatedAt)
                .last("LIMIT 1");
        ChatMemory chatMemory = this.getOne(queryWrapper);
        if (chatMemory == null){
            return null;
        }
        return chatMemory.getMessages();
    }

    @Override
    public void removeMessageById(Object memoryId) {
        Long userId = Long.valueOf(memoryId.toString());
        LambdaQueryWrapper<ChatMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMemory::getUserId, userId);
        this.remove(queryWrapper);
    }

    @Override
    public ChatMemory getByUserId(Long userId) {
        LambdaQueryWrapper<ChatMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMemory::getUserId, userId)
                .orderByDesc(ChatMemory::getUpdatedAt)
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}




