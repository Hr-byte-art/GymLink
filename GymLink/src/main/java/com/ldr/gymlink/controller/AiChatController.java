package com.ldr.gymlink.controller;

import com.ldr.gymlink.ai.aiService.GymLinkAiService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * AI 对话控制器
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
     * 流式对话接口 (SSE)
     */
    @Operation(summary = "流式对话（SSE）", description = "流式返回 AI 响应，支持工具调用")
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam Long userId, @RequestParam String message) {
        log.info("流式AI对话请求: userId={}, message={}", userId, message);
        String aiMessage = buildAiMessage(userId, message);

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

        aiService.gymLinkAiAssistantStream(userId, aiMessage)
                .onPartialResponse(partialResponse -> {
                    log.debug("[Stream] Partial response: {}", truncateResult(partialResponse));
                    sink.tryEmitNext(partialResponse);
                })
                .onRetrieved(contents -> log.info("[Stream] Retrieved {} contents",
                        contents != null ? contents.size() : 0))
                .onToolExecuted(toolExecution -> log.info("[Stream] Tool executed: name={}, result={}",
                        toolExecution.request().name(),
                        truncateResult(toolExecution.result())))
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
     * 同步对话接口
     */
    @Operation(summary = "同步对话", description = "同步返回 AI 响应，支持工具调用")
    @PostMapping("/chat")
    public String chat(@RequestParam Long userId, @RequestParam String message) {
        log.info("AI对话请求: userId={}, message={}", userId, message);
        String aiMessage = buildAiMessage(userId, message);

        try {
            GymLinkAiService aiService = AiServices.builder(GymLinkAiService.class)
                    .chatModel(qwenChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .chatMemoryStore(persistentChatMemoryStore)
                            .maxMessages(10)
                            .build())
                    .tools(gymLinkTools.getAllTools())
                    .build();

            return aiService.gymLinkAiAssistant(userId, aiMessage);
        } catch (Exception e) {
            log.error("AI对话异常", e);
            return "抱歉，我暂时无法回复，请稍后再试。";
        }
    }

    /**
     * 统一注入当前会话 userId，约束模型只能围绕当前用户做画像和推荐
     */
    private String buildAiMessage(Long userId, String message) {
        return """
                当前登录用户ID：%d
                使用规则：
                1. 如果需要调用当前用户画像或个性化推荐相关工具，必须使用这个 userId。
                2. 只能查询当前用户自己的数据，不能查询、假设或拼凑其他用户的信息。
                3. 当用户询问推荐课程、训练计划、饮食建议等个性化问题时，应优先结合当前用户画像工具结果来回答。

                用户原始消息：
                %s
                """.formatted(userId, message);
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
