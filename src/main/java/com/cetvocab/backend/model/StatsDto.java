package com.cetvocab.backend.model;

public record StatsDto(
        long total,
        long newCount,
        long dueNow,
        long known,
        long unknown
) {}