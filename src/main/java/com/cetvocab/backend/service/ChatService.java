package com.cetvocab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.ChatMessageMapper;
import com.cetvocab.backend.model.ChatMessageRow;

@Service
public class ChatService {

    private static final int MAX_HISTORY = 40;
    private static final int MAX_CONTENT_LENGTH = 8000;

    private final ChatMessageMapper chatMessageMapper;

    public ChatService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public void saveUserMessage(Long userId, Long wordId, String content) {
        save(userId, wordId, "user", content);
    }

    public void saveAssistantMessage(Long userId, Long wordId, String content) {
        save(userId, wordId, "assistant", content);
    }

    public List<ChatMessageRow> history(Long userId, Long wordId) {
        return chatMessageMapper.findRecent(userId, wordId, MAX_HISTORY);
    }

    private void save(Long userId, Long wordId, String role, String content) {
        if (content == null || content.isBlank()) return;
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            trimmed = trimmed.substring(0, MAX_CONTENT_LENGTH);
        }
        chatMessageMapper.insert(userId, wordId, role, trimmed);
    }
}