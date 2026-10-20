package com.cetvocab.backend.model;

public record LookupDto(
        String word,
        String phonetic,
        String translation,
        String tag
) {}