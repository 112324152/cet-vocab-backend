package com.cetvocab.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cetvocab.backend.client.LlmClient;
import com.cetvocab.backend.model.AskRequest;
import com.cetvocab.backend.model.ChatMessageRow;
import com.cetvocab.backend.security.CurrentUser;
import com.cetvocab.backend.service.ChatService;
import com.cetvocab.backend.service.WordService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final LlmClient llmClient;
    private final WordService wordService;
    private final ChatService chatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatController(LlmClient llmClient, WordService wordService, ChatService chatService) {
        this.llmClient = llmClient;
        this.wordService = wordService;
        this.chatService = chatService;
    }

    @PostMapping(value = "/words/{id}/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ask(@PathVariable Long id, @RequestBody AskRequest request) {
        Long userId = CurrentUser.id();
        SseEmitter emitter = new SseEmitter(0L);

        executor.execute(() -> {
            StringBuilder full = new StringBuilder();
            try {
                chatService.saveUserMessage(userId, id, request.question());

                String context = wordService.buildWordContext(id);
                List<Map<String, String>> messages = buildMessages(context, request.question());

                llmClient.streamChat(messages, delta -> {
                    full.append(delta);
                    sendQuietly(emitter, Map.of("delta", delta));
                });

                sendQuietly(emitter, Map.of("done", true));
            } catch (Exception e) {
                sendQuietly(emitter, Map.of("error",
                        e.getMessage() == null ? "unknown error" : e.getMessage()));
            } finally {
                if (full.length() > 0) {
                    chatService.saveAssistantMessage(userId, id, full.toString());
                }
                emitter.complete();
            }
        });

        return emitter;
    }

    @GetMapping("/words/{id}/chat")
    public List<ChatMessageRow> chatHistory(@PathVariable Long id) {
        Long userId = CurrentUser.id();
        return chatService.history(userId, id);
    }

    private List<Map<String, String>> buildMessages(String wordContext, String question) {
        String system = "你是英语学习助手。用户正在背四六级单词，请用中文回答，简洁准确，控制在 200 字以内。";
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
            // client disconnected, ignore
        }
    }
}