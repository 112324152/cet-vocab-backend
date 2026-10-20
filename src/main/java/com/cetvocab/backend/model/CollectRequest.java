package com.cetvocab.backend.model;

public record CollectRequest(
        String word,
        String context
) {}