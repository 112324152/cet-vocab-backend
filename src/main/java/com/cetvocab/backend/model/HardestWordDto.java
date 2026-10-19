package com.cetvocab.backend.model;

public record HardestWordDto(
        long wordId,
        String text,
        String phonetic,
        String firstMeaning,
        int lapses,
        long intervalMinutes,
        double easeFactor
) {}