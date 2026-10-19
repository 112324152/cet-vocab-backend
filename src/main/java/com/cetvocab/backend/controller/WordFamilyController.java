package com.cetvocab.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.model.WordFamilyDto;
import com.cetvocab.backend.service.WordFamilyService;

@RestController
@RequestMapping("/api")
public class WordFamilyController {

    private final WordFamilyService wordFamilyService;

    public WordFamilyController(WordFamilyService wordFamilyService) {
        this.wordFamilyService = wordFamilyService;
    }

    @GetMapping("/words/{id}/family")
    public WordFamilyDto family(@PathVariable Long id) {
        return wordFamilyService.getFamily(id);
    }
}