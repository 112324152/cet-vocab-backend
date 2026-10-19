package com.cetvocab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.WordFamilyMapper;
import com.cetvocab.backend.mapper.WordMapper;
import com.cetvocab.backend.model.WordFamilyDto;
import com.cetvocab.backend.model.WordFamilyRow;
import com.cetvocab.backend.model.WordRow;

@Service
public class WordFamilyService {

    private final WordFamilyMapper wordFamilyMapper;
    private final WordMapper wordMapper;

    public WordFamilyService(WordFamilyMapper wordFamilyMapper, WordMapper wordMapper) {
        this.wordFamilyMapper = wordFamilyMapper;
        this.wordMapper = wordMapper;
    }

    public WordFamilyDto getFamily(Long lemmaId) {
        WordRow lemma = wordMapper.findById(lemmaId);
        if (lemma == null) {
            throw new IllegalArgumentException("单词不存在: " + lemmaId);
        }
        List<WordFamilyRow> rows = wordFamilyMapper.findByLemmaId(lemmaId);
        List<WordFamilyDto.Member> members = rows.stream()
                .map(r -> new WordFamilyDto.Member(
                        r.getMemberText(),
                        r.getRelationType(),
                        r.getMemberPhonetic(),
                        r.getMemberMeaning()
                ))
                .toList();
        return new WordFamilyDto(lemmaId, lemma.getText(), members);
    }
}