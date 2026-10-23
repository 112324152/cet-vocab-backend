package com.cetvocab.backend.model;

import java.time.LocalDateTime;

public class ReviewLogRow {

    private Long wordId;
    private String state;
    private LocalDateTime reviewedAt;

    public Long getWordId() { return wordId; }
    public void setWordId(Long wordId) { this.wordId = wordId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}