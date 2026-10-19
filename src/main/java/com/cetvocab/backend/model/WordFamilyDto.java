package com.cetvocab.backend.model;

import java.util.List;

public record WordFamilyDto(
        long lemmaId,
        String lemma,
        List<Member> members
) {
    public record Member(
            String text,
            String relation,
            String phonetic,
            String meaning
    ) {}
}