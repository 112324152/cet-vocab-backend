package com.cetvocab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.WordMapper;
import com.cetvocab.backend.model.Word;
import com.cetvocab.backend.model.WordRow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class WordService {

    private final WordMapper wordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WordService(WordMapper wordMapper) {
        this.wordMapper = wordMapper;
    }

    public List<Word> getWords() {
        return wordMapper.findAll().stream()
                .map(this::toWord)
                .toList();
    }

    private Word toWord(WordRow row) {
        try {
            List<String> meanings = objectMapper.readValue(
                    row.getMeanings(),
                    new TypeReference<List<String>>() {}
            );
            return new Word(
                    row.getId(),
                    row.getText(),
                    row.getPhonetic(),
                    meanings,
                    row.getExample(),
                    row.getLevel()
            );
        } catch (Exception e) {
            throw new RuntimeException("解析释义失败: " + row.getText(), e);
        }
    }
}