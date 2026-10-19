package com.cetvocab.backend.seed;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cetvocab.backend.mapper.WordFamilyMapper;
import com.cetvocab.backend.mapper.WordMapper;
import com.cetvocab.backend.model.WordFamilyRow;
import com.cetvocab.backend.model.WordRow;
import com.opencsv.CSVReader;

@Component
public class WordFamilyImporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WordFamilyImporter.class);

    // ECDICT exchange 字段里可用的关系前缀
    private static final Set<String> VALID = Set.of("p", "d", "i", "3", "s", "r", "t");

    private final WordMapper wordMapper;
    private final WordFamilyMapper wordFamilyMapper;

    @Value("${ecdict.path:/data/ecdict.csv}")
    private String ecdictPath;

    public WordFamilyImporter(WordMapper wordMapper, WordFamilyMapper wordFamilyMapper) {
        this.wordMapper = wordMapper;
        this.wordFamilyMapper = wordFamilyMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        long existing = wordFamilyMapper.count();
        if (existing > 0) {
            log.info("word_family 已有 {} 条数据，跳过导入", existing);
            return;
        }

        File csv = new File(ecdictPath);
        if (!csv.exists()) {
            log.warn("ECDICT 文件不存在（{}），跳过词族导入。本地开发时正常。", ecdictPath);
            return;
        }

        log.info("开始导入派生词，源文件 {}", ecdictPath);

        // 1. 四六级词库 -> lemmaId 映射
        List<WordRow> allWords = wordMapper.findAll();
        Map<String, Long> lemmaToId = new HashMap<>();
        for (WordRow w : allWords) {
            lemmaToId.put(w.getText().toLowerCase(), w.getId());
        }
        log.info("四六级词库 {} 个词", lemmaToId.size());

        // 2. 第一遍：收集每个四六级词的变形
        // 结构：lemmaId -> (memberText -> relations)
        Map<Long, Map<String, Set<String>>> data = new HashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(csv))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 11) continue;
                String word = row[0].toLowerCase();
                Long lemmaId = lemmaToId.get(word);
                if (lemmaId == null) continue;

                String exchange = row[10];
                if (exchange == null || exchange.isEmpty()) continue;

                for (String part : exchange.split("/")) {
                    int colon = part.indexOf(':');
                    if (colon <= 0) continue;
                    String rel = part.substring(0, colon);
                    String member = part.substring(colon + 1).toLowerCase().trim();
                    if (member.isEmpty()) continue;
                    if (!VALID.contains(rel)) continue;

                    data.computeIfAbsent(lemmaId, k -> new HashMap<>())
                            .computeIfAbsent(member, k -> new TreeSet<>())
                            .add(rel);
                }
            }
        }

        int uniqueMembers = data.values().stream().mapToInt(Map::size).sum();
        log.info("第一遍扫描完成，涉及 {} 个四六级词，共 {} 条变形", data.size(), uniqueMembers);

        // 3. 第二遍：抓变形词的音标和释义
        Map<String, String[]> memberInfo = new HashMap<>();
        Map<String, Boolean> wanted = new HashMap<>();
        for (Map<String, Set<String>> members : data.values()) {
            for (String m : members.keySet()) {
                wanted.put(m, Boolean.TRUE);
            }
        }

        try (CSVReader reader = new CSVReader(new FileReader(csv))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 4) continue;
                String word = row[0].toLowerCase();
                if (!wanted.containsKey(word)) continue;
                memberInfo.put(word, new String[]{row[1], row[3]});
            }
        }
        log.info("第二遍扫描完成，查到 {} 条变形的音标释义", memberInfo.size());

        // 4. 写库
        int inserted = 0;
        for (Map.Entry<Long, Map<String, Set<String>>> entry : data.entrySet()) {
            Long lemmaId = entry.getKey();
            for (Map.Entry<String, Set<String>> me : entry.getValue().entrySet()) {
                String member = me.getKey();
                String relations = String.join(",", me.getValue());
                String[] info = memberInfo.getOrDefault(member, new String[]{"", ""});

                WordFamilyRow row = new WordFamilyRow();
                row.setLemmaId(lemmaId);
                row.setMemberText(member);
                row.setRelationType(relations);
                row.setMemberPhonetic(info[0]);
                row.setMemberMeaning(info[1]);
                wordFamilyMapper.insert(row);
                inserted++;
            }
        }
        log.info("导入完成，共写入 {} 条 word_family 记录", inserted);
    }
}