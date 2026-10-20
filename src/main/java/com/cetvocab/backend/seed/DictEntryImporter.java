package com.cetvocab.backend.seed;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.cetvocab.backend.mapper.DictEntryMapper;
import com.opencsv.CSVReader;

@Component
public class DictEntryImporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DictEntryImporter.class);

    // 这些 tag 说明是常见考试词汇或通用词汇，值得收录
    private static final Set<String> FLAG_TAGS = Set.of(
            "zk", "gk", "cet4", "cet6", "ky", "toefl", "ielts", "gre"
    );

    // 词频阈值：frq 或 bnc 数字越小越高频，超过这个值视为低频
    private static final int FREQ_THRESHOLD = 20000;

    private final DictEntryMapper dictEntryMapper;
    private final JdbcTemplate jdbcTemplate;

    @Value("${ecdict.path:/data/ecdict.csv}")
    private String ecdictPath;

    public DictEntryImporter(DictEntryMapper dictEntryMapper, JdbcTemplate jdbcTemplate) {
        this.dictEntryMapper = dictEntryMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        long existing = dictEntryMapper.count();
        if (existing > 0) {
            log.info("dict_entry 已有 {} 条数据，跳过导入", existing);
            return;
        }

        File csv = new File(ecdictPath);
        if (!csv.exists()) {
            log.warn("ECDICT 文件不存在（{}），跳过词典导入。本地开发时正常。", ecdictPath);
            return;
        }

        log.info("开始导入 ECDICT 高频词…");

        List<Object[]> batch = new ArrayList<>(2000);
        int total = 0;

        try (CSVReader reader = new CSVReader(new FileReader(csv))) {
            reader.readNext(); // 跳过表头
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 11) continue;
                String word = row[0];
                if (!isWanted(word, row[7], row[8], row[9])) continue;

                batch.add(new Object[]{
                        word,
                        nullSafe(row[1]),
                        nullSafe(row[3]),
                        nullSafe(row[2]),
                        nullSafe(row[7]),
                        nullSafe(row[10])
                });

                if (batch.size() >= 2000) {
                    total += flush(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) total += flush(batch);
        log.info("ECDICT 导入完成，共写入 {} 条", total);
    }

    private boolean isWanted(String word, String tag, String bnc, String frq) {
        if (word == null || word.isEmpty() || word.length() > 32) return false;

        // 跳过带空格、连字符、数字的复合词（先处理单一词）
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '\'')) {
                return false;
            }
        }

        if (tag != null && !tag.isEmpty()) {
            for (String t : tag.split(" ")) {
                if (FLAG_TAGS.contains(t)) return true;
            }
        }

        int frqNum = parseIntSafe(frq);
        if (frqNum > 0 && frqNum <= FREQ_THRESHOLD) return true;

        int bncNum = parseIntSafe(bnc);
        if (bncNum > 0 && bncNum <= FREQ_THRESHOLD) return true;

        return false;
    }

    private int flush(List<Object[]> batch) {
        jdbcTemplate.batchUpdate(
                "INSERT IGNORE INTO dict_entry (word, phonetic, translation, definition, tag, exchange) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                batch);
        return batch.size();
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}