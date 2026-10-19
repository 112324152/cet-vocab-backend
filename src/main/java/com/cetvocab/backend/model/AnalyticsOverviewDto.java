package com.cetvocab.backend.model;

public record AnalyticsOverviewDto(
        long totalWords,
        long learnedWords,
        long newWords,
        long totalReviews,
        long studyDays,
        long accuracyPercent,
        long dueNow
) {}