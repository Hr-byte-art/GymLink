package com.ldr.gymlink.controller;

import com.ldr.gymlink.ai.aiService.GymLinkAiService;
import com.ldr.gymlink.ai.aiService.GymLinkSyncAiService;
import com.ldr.gymlink.ai.tools.GymLinkTools;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * @Author 王哈哈
 * @Date 2025/12/14 22:52:04
 * @Description AI对话控制（集成 Tools 工具调用）
 */
@RestController
@RequestMapping("/ai")
@Slf4j
@Tag(name = "AI对话")
public class AiChatController {

    @Resource
    private QwenStreamingChatModel qwenStreamingChatModel;

    @Resource
    private QwenChatModel qwenChatModel;

    @Resource(name = "persistentChatMemoryStore")
    private ChatMemoryStore persistentChatMemoryStore;

    @Resource
    private GymLinkTools gymLinkTools;

    /**
     * 流式对话接口 (SSE) - 支持 Tools 工具调用
     */
    @Operation(summary = "流式对话（SSE）", description = "流式返回AI响应，支持工具调用")
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam Long userId, @RequestParam String message) {
        log.info("流式AI对话请求: userId={}, message={}", userId, message);

        // 构建支持 Tools 的流式 AI 服务
        GymLinkAiService aiService = AiServices.builder(GymLinkAiService.class)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(persistentChatMemoryStore)
                        .maxMessages(10)
                        .build())
                .tools(gymLinkTools.getAllTools())
                .build();

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        aiService.gymLinkAiAssistant(userId, message)
                .onPartialResponse(partialResponse -> {
                    // 流式输出部分响应
                    log.debug("[Stream] Partial response: {}", truncateResult(partialResponse));
                    sink.tryEmitNext(partialResponse);
                })
                .onRetrieved(contents -> {
                    // RAG 检索回调（如果有）
                    log.info("[Stream] Retrieved {} contents", contents != null ? contents.size() : 0);
                })
                .onToolExecuted(toolExecution -> {
                    // 工具执行完成后的回调
                    log.info("[Stream] Tool executed: name={}, result={}",
                            toolExecution.request().name(),
                            truncateResult(toolExecution.result()));
                })
                .onCompleteResponse(response -> {
                    log.info("[Stream] Complete response: userId={}, content length={}",
                            userId,
                            response.aiMessage() != null && response.aiMessage().text() != null
                                    ? response.aiMessage().text().length() : 0);
                    sink.tryEmitComplete();
                })
                .onError(error -> {
                    log.error("[Stream] Error occurred: userId={}, errorType={}, message={}",
                            userId,
                            error.getClass().getSimpleName(),
                            error.getMessage(),
                            error);
                    sink.tryEmitError(error);
                })
                .start();
        
        log.info("[Stream] TokenStream started for userId={}", userId);

        return sink.asFlux();
    }

    /**
     * 同步对话接口（支持 Tools 工具调用）
     */
    @Operation(summary = "同步对话（支持工具调用）", description = "同步返回AI响应，支持调用系统工具查询课程、教练、器材、食谱等信息")
    @PostMapping("/chat")
    public String chat(@RequestParam Long userId, @RequestParam String message) {
        log.info("AI对话请求: userId={}, message={}", userId, message);

        try {
            // 使用同步 AI 服务（返回 String，支持 Tools）
            // 注册所有分类工具
            GymLinkSyncAiService aiService = AiServices.builder(GymLinkSyncAiService.class)
                    .chatModel(qwenChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .chatMemoryStore(persistentChatMemoryStore)
                            .maxMessages(10)
                            .build())
                    .tools(gymLinkTools.getAllTools())
                    .build();

            return aiService.gymLinkAiAssistant(userId, message);
        } catch (Exception e) {
            log.error("AI对话异常", e);
            return "抱歉，我暂时无法回复，请稍后再试。";
        }
    }

    /**
     * 截断工具执行结果，避免日志过长
     */
    private String truncateResult(String result) {
        if (result == null) {
            return "null";
        }
        if (result.length() > 200) {
            return result.substring(0, 200) + "...(truncated)";
        }
        return result;
    }
}
