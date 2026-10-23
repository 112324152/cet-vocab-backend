package com.cetvocab.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cetvocab.backend.client.LlmClient;
import com.cetvocab.backend.model.TranslateRequest;

@RestController
@RequestMapping("/api")
public class TranslateController {

    private static final int MAX_LENGTH = 2000;

    private final LlmClient llmClient;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public TranslateController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @PostMapping(value = "/translate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter translate(@RequestBody TranslateRequest request) {
        String text = request.text() == null ? "" : request.text().trim();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text 不能为空");
        }
        if (text.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "text 超过 " + MAX_LENGTH + " 字符");
        }

        SseEmitter emitter = new SseEmitter(0L);

        executor.execute(() -> {
            try {
                List<Map<String, String>> messages = List.of(
                        Map.of("role", "system", "content",
                                "你是专业的英译中翻译器。只输出中文译文，不要解释、不要加引号、"
                                        + "不要重复原文、不要加任何前后缀说明。保持原文的段落和标点。"),
                        Map.of("role", "user", "content", text)
                );
                llmClient.streamChat(messages,
                        delta -> sendQuietly(emitter, Map.of("delta", delta)));
                sendQuietly(emitter, Map.of("done", true));
            } catch (Exception e) {
                sendQuietly(emitter, Map.of("error",
                        e.getMessage() == null ? "unknown error" : e.getMessage()));
            } finally {
                emitter.complete();
            }
        });

        return emitter;
    }

    private void sendQuietly(SseEmitter emitter, Object data) {
        try {
            emitter.send(data);
        } catch (Exception e) {
            // 客户端断开时忽略
        }
    }
}