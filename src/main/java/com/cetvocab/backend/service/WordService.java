package com.cetvocab.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.WordMapper;
import com.cetvocab.backend.mapper.WordStateMapper;
import com.cetvocab.backend.model.StatsDto;
import com.cetvocab.backend.model.Word;
import com.cetvocab.backend.model.WordRow;
import com.cetvocab.backend.model.WordStateRow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WordService {

    private final WordMapper wordMapper;
    private final WordStateMapper wordStateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public WordService(WordMapper wordMapper, WordStateMapper wordStateMapper) {
        this.wordMapper = wordMapper;
        this.wordStateMapper = wordStateMapper;
    }

    public List<Word> getWords() {
        return wordMapper.findAll().stream()
                .map(this::toWord)
                .toList();
    }

    public Word next(Long excludeId) {
        List<WordRow> all = wordMapper.findAll();
        if (all.isEmpty()) {
            throw new IllegalStateException("词库为空");
        }

        Map<Long, String> stateMap = wordStateMapper.findAll().stream()
                .collect(Collectors.toMap(WordStateRow::getWordId, WordStateRow::getState));

        List<WordRow> candidates = all.stream()
                .filter(r -> !r.getId().equals(excludeId))
                .toList();
        if (candidates.isEmpty()) {
            candidates = all;
        }

        int totalWeight = candidates.stream()
                .mapToInt(r -> weightOf(stateMap.get(r.getId())))
                .sum();

        int roll = random.nextInt(totalWeight);
        for (WordRow row : candidates) {
            roll -= weightOf(stateMap.get(row.getId()));
            if (roll < 0) {
                return toWord(row);
            }
        }
        return toWord(candidates.get(candidates.size() - 1));
    }

    public void markState(Long wordId, String state) {
        if (!"known".equals(state) && !"unknown".equals(state)) {
            throw new IllegalArgumentException("state 只能是 known 或 unknown");
        }
        wordStateMapper.upsert(wordId, state);
    }

    public StatsDto stats() {

        return new StatsDto(
                wordStateMapper.countTotal(),
                wordStateMapper.countMarked(),
                wordStateMapper.countKnown(),
                wordStateMapper.countUnknown()
        );
    }
    // 把单词信息拼成一段文字，给模型当上下文
    public String buildWordContext(Long id) {
        WordRow row = wordMapper.findById(id);
        if (row == null) {
            throw new IllegalArgumentException("单词不存在: " + id);
        }
        Word word = toWord(row);
        return String.format(
                "%s %s；释义：%s；例句：%s",
                word.getText(),
                word.getPhonetic(),
                String.join("；", word.getMeanings()),
                word.getExample()
        );
    }
    private int weightOf(String state) {
        if ("unknown".equals(state)) return 4;
        if ("known".equals(state)) return 1;
        return 2;
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