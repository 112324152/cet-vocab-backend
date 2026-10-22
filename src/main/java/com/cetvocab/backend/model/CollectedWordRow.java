package com.cetvocab.backend.model;

import java.time.LocalDateTime;

public class CollectedWordRow {

    private Long id;
    private Long userId;
    private String word;
    private String phonetic;
    private String translation;
    private String context;
    private LocalDateTime collectedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
}