package com.cetvocab.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cetvocab.backend.client.LlmClient;
import com.cetvocab.backend.model.AskRequest;
import com.cetvocab.backend.service.WordService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final LlmClient llmClient;
    private final WordService wordService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatController(LlmClient llmClient, WordService wordService) {
        this.llmClient = llmClient;
        this.wordService = wordService;
    }

    @PostMapping(value = "/words/{id}/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ask(@PathVariable Long id, @RequestBody AskRequest request) {
        // 0L 表示不超时：本地模型首次加载要几十秒，默认 30 秒会被掐断
        SseEmitter emitter = new SseEmitter(0L);

        // 请求线程在这里立刻返回 emitter，不阻塞。
        // 真正的干活（等模型、逐块转发）在这个独立线程里。
        executor.execute(() -> {
            try {
                String context = wordService.buildWordContext(id);
                List<Map<String, String>> messages = buildMessages(context, request.question());

                llmClient.streamChat(messages, delta -> sendQuietly(emitter, Map.of("delta", delta)));

                sendQuietly(emitter, Map.of("done", true));
                emitter.complete();
            } catch (Exception e) {
                sendQuietly(emitter, Map.of("error", e.getMessage() == null ? "未知错误" : e.getMessage()));
                emitter.complete();
            }
        });

        return emitter;
    }

    private List<Map<String, String>> buildMessages(String wordContext, String question) {
        String system = "你是一个英语学习助手。用户正在背四六级单词，请用中文回答，简洁准确，控制在 200 字以内。";
        String user = "我正在学的单词：" + wordContext + "\n\n我的问题：" + question;
        return List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
        );
    }

    private void sendQuietly(SseEmitter emitter, Object data) {
        try {
            emitter.send(data);
        } catch (Exception e) {
            // 客户端断开时 send 会抛异常，忽略即可
        }
    }
}