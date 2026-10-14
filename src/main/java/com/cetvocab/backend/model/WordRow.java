package com.cetvocab.backend.model;

public class WordRow {

    private Long id;
    private String text;
    private String phonetic;
    private String meanings; // 存的是 JSON 字符串
    private String example;
    private String level;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getMeanings() { return meanings; }
    public void setMeanings(String meanings) { this.meanings = meanings; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}