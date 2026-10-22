package com.cetvocab.backend.service;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.MasteryMapper;

@Service
public class MasteryService {

    private final MasteryMapper masteryMapper;

    public MasteryService(MasteryMapper masteryMapper) {
        this.masteryMapper = masteryMapper;
    }

    public void markMastered(Long userId, Long wordId) {
        masteryMapper.markMastered(userId, wordId);
    }
}