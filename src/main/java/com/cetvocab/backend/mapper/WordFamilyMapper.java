package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.WordFamilyRow;

@Mapper
public interface WordFamilyMapper {

    @Select("SELECT COUNT(*) FROM word_family")
    long count();

    @Select("SELECT id, lemma_id, member_text, relation_type, member_phonetic, member_meaning "
            + "FROM word_family WHERE lemma_id = #{lemmaId} "
            + "ORDER BY relation_type, member_text")
    List<WordFamilyRow> findByLemmaId(@Param("lemmaId") Long lemmaId);

    @Insert("INSERT INTO word_family (lemma_id, member_text, relation_type, member_phonetic, member_meaning) "
            + "VALUES (#{lemmaId}, #{memberText}, #{relationType}, #{memberPhonetic}, #{memberMeaning})")
    void insert(WordFamilyRow row);
}