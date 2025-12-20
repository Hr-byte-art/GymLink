package com.ldr.gymlink.ai.aiService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @Author 王哈哈
 * @Date 2025/12/15
 * @Description GymLink 同步 AI 服务接口（支持 Tools）
 */
public interface GymLinkSyncAiService {

    @SystemMessage(fromResource = "prompt/ai-assistant-prompt.md")
    String gymLinkAiAssistant(@MemoryId Long memoryId, @UserMessage String userMessage);
}
