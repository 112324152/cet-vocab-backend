package com.cetvocab.backend.model;

import java.util.List;

public class Word {

    private Long id;
    private String text;
    private String phonetic;
    private List<String> meanings;
    private String example;
    private String level;

    public Word(Long id, String text, String phonetic,
                List<String> meanings, String example, String level) {
        this.id = id;
        this.text = text;
        this.phonetic = phonetic;
        this.meanings = meanings;
        this.example = example;
        this.level = level;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getPhonetic() { return phonetic; }
    public List<String> getMeanings() { return meanings; }
    public String getExample() { return example; }
    public String getLevel() { return level; }
}