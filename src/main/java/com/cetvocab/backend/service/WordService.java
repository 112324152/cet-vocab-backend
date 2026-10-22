package com.cetvocab.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.ReviewLogMapper;
import com.cetvocab.backend.mapper.WordMapper;
import com.cetvocab.backend.mapper.WordStateMapper;
import com.cetvocab.backend.model.AnalyticsOverviewDto;
import com.cetvocab.backend.model.DailyStatDto;
import com.cetvocab.backend.model.HardestWordDto;
import com.cetvocab.backend.model.IntervalBucketDto;
import com.cetvocab.backend.model.StatsDto;
import com.cetvocab.backend.model.Word;
import com.cetvocab.backend.model.WordRow;
import com.cetvocab.backend.model.WordStateRow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WordService {

    private static final double DEFAULT_EASE = 2.5;
    private static final double MIN_EASE = 1.3;
    private static final double MAX_EASE = 2.8;
    private static final double EASE_UP = 0.05;
    private static final double EASE_DOWN = 0.2;
    private static final long FIRST_INTERVAL_MIN = 1440;
    private static final long SECOND_INTERVAL_MIN = 4320;
    private static final long RELEARN_INTERVAL_MIN = 10;
    private static final long MAX_INTERVAL_MIN = 525600;

    private final WordMapper wordMapper;
    private final WordStateMapper wordStateMapper;
    private final ReviewLogMapper reviewLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WordService(WordMapper wordMapper,
                       WordStateMapper wordStateMapper,
                       ReviewLogMapper reviewLogMapper) {
        this.wordMapper = wordMapper;
        this.wordStateMapper = wordStateMapper;
        this.reviewLogMapper = reviewLogMapper;
    }

    public List<Word> getWords() {
        return wordMapper.findAll().stream()
                .map(this::toWord)
                .toList();
    }

    public String getWordText(Long id) {
        return getWord(id).getText();
    }

    public Word next(Long userId, Long excludeId) {
        Long excluded = (excludeId == null) ? -1L : excludeId;

        Long dueId = wordStateMapper.findMostOverdue(userId, excluded);
        if (dueId != null) {
            return getWord(dueId);
        }

        Long newId = wordMapper.findRandomNew(userId, excluded);
        if (newId != null) {
            return getWord(newId);
        }

        Long soonestId = wordStateMapper.findSoonest(userId, excluded);
        if (soonestId == null) {
            throw new IllegalStateException("词库为空");
        }
        return getWord(soonestId);
    }

    public void markState(Long userId, Long wordId, String state) {
        if (!"known".equals(state) && !"unknown".equals(state)) {
            throw new IllegalArgumentException("state 只能是 known 或 unknown");
        }
        WordStateRow existing = wordStateMapper.findByWordId(userId, wordId);
        WordStateRow updated = applyAlgorithm(userId, existing, wordId, state);
        wordStateMapper.upsert(updated);
        reviewLogMapper.insert(userId, wordId, state);
    }

    private WordStateRow applyAlgorithm(Long userId, WordStateRow existing, Long wordId, String state) {
        double ease = (existing == null) ? DEFAULT_EASE : existing.getEaseFactor();
        long interval = (existing == null) ? 0 : existing.getIntervalMinutes();
        int reps = (existing == null) ? 0 : existing.getRepetitions();
        int lapses = (existing == null) ? 0 : existing.getLapses();

        if ("unknown".equals(state)) {
            reps = 0;
            lapses += 1;
            ease = Math.max(MIN_EASE, ease - EASE_DOWN);
            interval = RELEARN_INTERVAL_MIN;
        } else {
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
        row.setUserId(userId);
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

    public StatsDto stats(Long userId) {
        long total = wordMapper.countTotal();
        long due = wordStateMapper.countDue(userId);
        long known = wordStateMapper.countKnown(userId);
        long unknown = wordStateMapper.countUnknown(userId);
        long learned = known + unknown;
        long newCount = Math.max(0, total - learned);
        return new StatsDto(total, newCount, due, known, unknown);
    }

    public AnalyticsOverviewDto analyticsOverview(Long userId) {
        long totalWords = wordMapper.countTotal();
        long learned = wordStateMapper.countKnown(userId) + wordStateMapper.countUnknown(userId);
        long newWords = Math.max(0, totalWords - learned);
        long totalReviews = reviewLogMapper.countTotal(userId);
        long knownReviews = reviewLogMapper.countKnown(userId);
        long studyDays = reviewLogMapper.countStudyDays(userId);
        long accuracy = totalReviews == 0 ? 0 : Math.round(knownReviews * 100.0 / totalReviews);
        long dueNow = wordStateMapper.countDue(userId);
        return new AnalyticsOverviewDto(
                totalWords, learned, newWords, totalReviews, studyDays, accuracy, dueNow);
    }

    public List<DailyStatDto> dailyStats(Long userId, int days) {
        return reviewLogMapper.findDaily(userId, days);
    }

    public List<HardestWordDto> hardestWords(Long userId, int limit) {
        List<WordStateRow> states = wordStateMapper.findHardest(userId, limit);
        List<HardestWordDto> result = new ArrayList<>();
        for (WordStateRow s : states) {
            WordRow row = wordMapper.findById(s.getWordId());
            if (row == null) continue;
            String firstMeaning = "";
            try {
                List<String> meanings = objectMapper.readValue(
                        row.getMeanings(),
                        new TypeReference<List<String>>() {});
                if (!meanings.isEmpty()) firstMeaning = meanings.get(0);
            } catch (Exception ignored) {
            }
            result.add(new HardestWordDto(
                    row.getId(),
                    row.getText(),
                    row.getPhonetic(),
                    firstMeaning,
                    s.getLapses(),
                    s.getIntervalMinutes(),
                    s.getEaseFactor()
            ));
        }
        return result;
    }

    public List<IntervalBucketDto> intervalDistribution(Long userId) {
        long below1h = wordStateMapper.countByIntervalRange(userId, 0, 59);
        long oneTo24h = wordStateMapper.countByIntervalRange(userId, 60, 1439);
        long oneTo7d = wordStateMapper.countByIntervalRange(userId, 1440, 10079);
        long sevenTo30d = wordStateMapper.countByIntervalRange(userId, 10080, 43199);
        long over30d = wordStateMapper.countByIntervalRange(userId, 43200, Long.MAX_VALUE);
        return List.of(
                new IntervalBucketDto("10 分钟", below1h),
                new IntervalBucketDto("1 小时内", oneTo24h),
                new IntervalBucketDto("1-7 天", oneTo7d),
                new IntervalBucketDto("7-30 天", sevenTo30d),
                new IntervalBucketDto("30 天以上", over30d)
        );
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