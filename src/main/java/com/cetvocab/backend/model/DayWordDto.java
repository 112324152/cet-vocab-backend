package com.cetvocab.backend.model;

import java.time.LocalDateTime;

public record DayWordDto(
        Long wordId,
        String text,
        String phonetic,
        String firstMeaning,
        String state,
        LocalDateTime reviewedAt
) {}