package com.cetvocab.backend.seed;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WordImporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WordImporter.class);

    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${import.enabled:false}")
    private boolean enabled;

    public WordImporter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            return;
        }

        long existing = countExisting();
        if (existing > 0) {
            log.info("词库已有 {} 条数据，跳过导入", existing);
            return;
        }

        log.info("开始导入词库...");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:seed/*.json");

        if (resources.length == 0) {
            log.warn("没有在 src/main/resources/seed/ 下找到 JSON 文件，跳过导入");
            return;
        }

        // 一个单词可能同时出现在 CET4 和 CET6 里，用 text 合并，级别并起来
        Map<String, SeedWord> merged = new HashMap<>();

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String level = levelOf(filename);
            if (level == null) {
                continue;
            }

            try (InputStream in = resource.getInputStream()) {
                List<Map<String, Object>> items =
                        objectMapper.readValue(in, new TypeReference<>() {});
                for (Map<String, Object> item : items) {
                    String name = String.valueOf(item.getOrDefault("name", "")).trim();
                    if (name.isEmpty()) {
                        continue;
                    }

                    String phonetic = asText(item.get("usphone"));
                    if (phonetic.isEmpty()) {
                        phonetic = asText(item.get("ukphone"));
                    }

                    @SuppressWarnings("unchecked")
                    List<String> trans = (List<String>) item.getOrDefault("trans", List.of());

                    mergeInto(merged, name, phonetic, trans, level);
                }
            }
            log.info("处理 {} 完成", filename);
        }

        List<SeedWord> words = new ArrayList<>(merged.values());
        log.info("去重后 {} 个单词，准备写库", words.size());

        String sql = "INSERT INTO word (text, phonetic, meanings, example, level) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE phonetic = VALUES(phonetic), "
                + "meanings = VALUES(meanings), level = VALUES(level)";

        try (var conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (SeedWord w : words) {
                ps.setString(1, w.text);
                ps.setString(2, w.phonetic);
                ps.setString(3, objectMapper.writeValueAsString(w.trans));
                ps.setString(4, "");
                ps.setString(5, String.join(",", w.levels));
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            conn.commit();
            log.info("导入完成，影响 {} 条", counts.length);
        }

        log.info("本次为一次性操作，请把 application.yml 里的 import.enabled 改回 false 再重启");
    }

    /**
     * 查当前词库条数。用于"有数据就跳过导入"，让导入开关在 Docker 里可以常开。
     * 依赖 word 表已存在。
     */
    private long countExisting() throws Exception {
        try (var conn = dataSource.getConnection();
             var st = conn.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM word")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private void mergeInto(Map<String, SeedWord> merged, String name,
                           String phonetic, List<String> trans, String level) {
        String key = name.toLowerCase();
        SeedWord word = merged.get(key);
        if (word == null) {
            word = new SeedWord(name, phonetic, trans);
            merged.put(key, word);
        } else if (word.phonetic.isEmpty() && !phonetic.isEmpty()) {
            word.phonetic = phonetic;
        }
        word.addLevel(level);
    }

    private String levelOf(String filename) {
        if (filename == null) return null;
        if (filename.startsWith("CET4")) return "CET4";
        if (filename.startsWith("CET6")) return "CET6";
        return null;
    }

    private String asText(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o).trim();
        return "null".equals(s) ? "" : s;
    }

    private static class SeedWord {
        final String text;
        String phonetic;
        final List<String> trans;
        final Set<String> levels = new TreeSet<>();

        SeedWord(String text, String phonetic, List<String> trans) {
            this.text = text;
            this.phonetic = phonetic;
            this.trans = trans;
        }

        void addLevel(String level) {
            this.levels.add(level);
        }
    }
}