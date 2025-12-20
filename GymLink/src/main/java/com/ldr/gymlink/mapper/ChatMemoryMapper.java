package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.ChatMemory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【chat_memory(AI助手对话记忆表)】的数据库操作Mapper
* @createDate 2025-12-14 22:26:21
* @Entity com.ldr.gymlink.model.entity.ChatMemory
*/
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemory> {

}




