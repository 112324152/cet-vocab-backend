package com.cetvocab.backend.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.client.DictLookupClient;
import com.cetvocab.backend.mapper.CollectedWordMapper;
import com.cetvocab.backend.mapper.DictEntryMapper;
import com.cetvocab.backend.model.CollectRequest;
import com.cetvocab.backend.model.CollectedWordRow;
import com.cetvocab.backend.model.DictEntryRow;
import com.cetvocab.backend.model.LookupDto;

@Service
public class DictService {

    private final DictEntryMapper dictEntryMapper;
    private final CollectedWordMapper collectedWordMapper;
    private final DictLookupClient dictLookupClient;

    public DictService(DictEntryMapper dictEntryMapper,
                       CollectedWordMapper collectedWordMapper,
                       DictLookupClient dictLookupClient) {
        this.dictEntryMapper = dictEntryMapper;
        this.collectedWordMapper = collectedWordMapper;
        this.dictLookupClient = dictLookupClient;
    }

    public LookupDto lookup(String rawWord) {
        if (rawWord == null) return null;
        String word = rawWord.toLowerCase().trim();
        if (word.isEmpty() || word.length() > 64) return null;
        DictEntryRow row = dictEntryMapper.findByWord(word);
        return row == null ? null : toDto(row);
    }

    public List<LookupDto> batchLookup(List<String> rawWords) {
        if (rawWords == null || rawWords.isEmpty()) return List.of();
        Set<String> cleaned = new LinkedHashSet<>();
        for (String w : rawWords) {
            if (w == null) continue;
            String t = w.toLowerCase().trim();
            if (t.isEmpty() || t.length() > 64) continue;
            cleaned.add(t);
            if (cleaned.size() >= 500) break;
        }
        if (cleaned.isEmpty()) return List.of();
        List<DictEntryRow> rows = dictEntryMapper.findByWords(new ArrayList<>(cleaned));
        List<LookupDto> result = new ArrayList<>();
        for (DictEntryRow row : rows) result.add(toDto(row));
        return result;
    }

    public void collect(CollectRequest request) {
        if (request == null || request.word() == null) {
            throw new IllegalArgumentException("word 必须非空");
        }
        String word = request.word().toLowerCase().trim();
        if (word.isEmpty() || word.length() > 64) {
            throw new IllegalArgumentException("word 长度非法");
        }

        DictEntryRow dict = dictEntryMapper.findByWord(word);
        String phonetic = (dict == null) ? "" : emptySafe(dict.getPhonetic());
        String translation = (dict == null) ? "" : emptySafe(dict.getTranslation());

        if (translation.isEmpty()) {
            translation = dictLookupClient.lookupTranslation(word);
        }

        CollectedWordRow row = new CollectedWordRow();
        row.setWord(word);
        row.setPhonetic(phonetic);
        row.setTranslation(translation);
        row.setContext(request.context() == null ? "" : request.context().trim());
        collectedWordMapper.upsert(row);
    }

    public void deleteCollected(Long id) {
        collectedWordMapper.deleteById(id);
    }

    public List<CollectedWordRow> listCollected() {
        return collectedWordMapper.findAll();
    }

    private String emptySafe(String s) {
        return s == null ? "" : s;
    }

    private LookupDto toDto(DictEntryRow row) {
        String raw = emptySafe(row.getTranslation());
        String first = raw.split("\\\\n")[0];
        return new LookupDto(
                row.getWord(),
                emptySafe(row.getPhonetic()),
                first,
                emptySafe(row.getTag())
        );
    }
}
