package com.ldr.gymlink.ai.aiService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * @Author 王哈哈
 * @Date 2025/12/14 20:02:19
 * @Description GymLink AI 服务接口
 * 注意：不使用 @AiService 注解，改为在 Controller 中手动构建，以便集成 Tools
 */
public interface GymLinkAiService {

    @SystemMessage(fromResource = "prompt/ai-assistant-prompt.md")
    TokenStream gymLinkAiAssistant(@MemoryId Long memoryId, @UserMessage String userMessage);
}
