package com.cetvocab.backend.model;

import java.time.LocalDateTime;

public class WordStateRow {

    private Long wordId;
    private String state;
    private double easeFactor;
    private long intervalMinutes;
    private int repetitions;
    private int lapses;
    private LocalDateTime nextReviewAt;
    private LocalDateTime lastReviewedAt;

    public Long getWordId() { return wordId; }
    public void setWordId(Long wordId) { this.wordId = wordId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

    public long getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(long intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public int getLapses() { return lapses; }
    public void setLapses(int lapses) { this.lapses = lapses; }

    public LocalDateTime getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(LocalDateTime nextReviewAt) { this.nextReviewAt = nextReviewAt; }

    public LocalDateTime getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(LocalDateTime lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }
}