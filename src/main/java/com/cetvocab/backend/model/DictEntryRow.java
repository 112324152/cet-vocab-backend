package com.cetvocab.backend.model;

public class DictEntryRow {

    private Long id;
    private String word;
    private String phonetic;
    private String translation;
    private String definition;
    private String tag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}