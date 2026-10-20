package com.cetvocab.backend.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.service.MasteryService;

@RestController
@RequestMapping("/api/analytics")
public class MasteryController {

    private final MasteryService masteryService;

    public MasteryController(MasteryService masteryService) {
        this.masteryService = masteryService;
    }

    @PostMapping("/mastered/{wordId}")
    public void markMastered(@PathVariable Long wordId) {
        masteryService.markMastered(wordId);
    }
}
