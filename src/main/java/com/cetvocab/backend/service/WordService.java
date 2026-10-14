package com.cetvocab.backend.service;

import java.time.LocalDateTime;
import java.util.List;

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

    // SM-2 参数。想调学习强度，改这几个常量就够了。
    private static final double DEFAULT_EASE = 2.5;
    private static final double MIN_EASE = 1.3;
    private static final double MAX_EASE = 2.8;
    private static final double EASE_UP = 0.05;
    private static final double EASE_DOWN = 0.2;
    private static final long FIRST_INTERVAL_MIN = 1440;      // 1 天
    private static final long SECOND_INTERVAL_MIN = 4320;     // 3 天
    private static final long RELEARN_INTERVAL_MIN = 10;      // 答错后 10 分钟重见
    private static final long MAX_INTERVAL_MIN = 525600;      // 上限 365 天

    private final WordMapper wordMapper;
    private final WordStateMapper wordStateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WordService(WordMapper wordMapper, WordStateMapper wordStateMapper) {
        this.wordMapper = wordMapper;
        this.wordStateMapper = wordStateMapper;
    }

    /**
     * 抽下一个词。三级优先：
     * 1. 有到期的 → 最逾期优先
     * 2. 没有到期的 → 抽一个没学过的新词
     * 3. 全学过且都没到期 → 取最近会到期的
     */
    public Word next(Long excludeId) {
        Long excluded = (excludeId == null) ? -1L : excludeId;

        Long dueId = wordStateMapper.findMostOverdue(excluded);
        if (dueId != null) {
            return getWord(dueId);
        }

        Long newId = wordMapper.findRandomNew(excluded);
        if (newId != null) {
            return getWord(newId);
        }

        Long soonestId = wordStateMapper.findSoonest(excluded);
        if (soonestId == null) {
            throw new IllegalStateException("词库为空");
        }
        return getWord(soonestId);
    }

    /**
     * 标记一个词，并按 SM-2 规则更新它的复习计划。
     */
    public void markState(Long wordId, String state) {
        if (!"known".equals(state) && !"unknown".equals(state)) {
            throw new IllegalArgumentException("state 只能是 known 或 unknown");
        }
        WordStateRow existing = wordStateMapper.findByWordId(wordId);
        WordStateRow updated = applyAlgorithm(existing, wordId, state);
        wordStateMapper.upsert(updated);
    }

    private WordStateRow applyAlgorithm(WordStateRow existing, Long wordId, String state) {
        double ease = (existing == null) ? DEFAULT_EASE : existing.getEaseFactor();
        long interval = (existing == null) ? 0 : existing.getIntervalMinutes();
        int reps = (existing == null) ? 0 : existing.getRepetitions();
        int lapses = (existing == null) ? 0 : existing.getLapses();

        if ("unknown".equals(state)) {
            // 忘了：间隔归零到 10 分钟，难度上调
            reps = 0;
            lapses += 1;
            ease = Math.max(MIN_EASE, ease - EASE_DOWN);
            interval = RELEARN_INTERVAL_MIN;
        } else {
            // 记得：间隔拉长
            if (reps == 0) {
                interval = FIRST_INTERVAL_MIN;
            } else if (reps == 1) {
                interval = SECOND_INTERVAL_MIN;
            } else {
                interval = Math.round(interval * ease);
            }
            reps += 1;
            ease = Math.min(MAX_EASE, ease + EASE_UP);
            interval = Math.min(MAX_INTERVAL_MIN, interval);
        }

        LocalDateTime now = LocalDateTime.now();
        WordStateRow row = new WordStateRow();
        row.setWordId(wordId);
        row.setState(state);
        row.setEaseFactor(ease);
        row.setIntervalMinutes(interval);
        row.setRepetitions(reps);
        row.setLapses(lapses);
        row.setLastReviewedAt(now);
        row.setNextReviewAt(now.plusMinutes(interval));
        return row;
    }
    /** 返回整份词表，仅用于调试和后续的词库管理页面。正式背诵走 next()。 */
    public List<Word> getWords() {
        return wordMapper.findAll().stream()
                .map(this::toWord)
                .toList();
    }
    public StatsDto stats() {
        long total = wordMapper.countTotal();
        long due = wordStateMapper.countDue();
        long known = wordStateMapper.countKnown();
        long unknown = wordStateMapper.countUnknown();
        long learned = known + unknown;
        long newCount = Math.max(0, total - learned);
        return new StatsDto(total, newCount, due, known, unknown);
    }

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

    private Word getWord(Long id) {
        WordRow row = wordMapper.findById(id);
        if (row == null) {
            throw new IllegalStateException("单词不存在: " + id);
        }
        return toWord(row);
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