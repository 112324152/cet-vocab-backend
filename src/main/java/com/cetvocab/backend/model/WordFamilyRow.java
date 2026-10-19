package com.cetvocab.backend.model;

public class WordFamilyRow {

    private Long id;
    private Long lemmaId;
    private String memberText;
    private String relationType;
    private String memberPhonetic;
    private String memberMeaning;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLemmaId() { return lemmaId; }
    public void setLemmaId(Long lemmaId) { this.lemmaId = lemmaId; }

    public String getMemberText() { return memberText; }
    public void setMemberText(String memberText) { this.memberText = memberText; }

    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }

    public String getMemberPhonetic() { return memberPhonetic; }
    public void setMemberPhonetic(String memberPhonetic) { this.memberPhonetic = memberPhonetic; }

    public String getMemberMeaning() { return memberMeaning; }
    public void setMemberMeaning(String memberMeaning) { this.memberMeaning = memberMeaning; }
}