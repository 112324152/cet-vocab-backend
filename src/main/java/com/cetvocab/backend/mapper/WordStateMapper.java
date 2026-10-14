package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.WordStateRow;

@Mapper
public interface WordStateMapper {

    @Select("SELECT word_id, state FROM word_state")
    List<WordStateRow> findAll();

    @Insert("INSERT INTO word_state (word_id, state) VALUES (#{wordId}, #{state}) "
            + "ON DUPLICATE KEY UPDATE state = #{state}")
    void upsert(@Param("wordId") Long wordId, @Param("state") String state);

    @Select("SELECT COUNT(*) FROM word")
    long countTotal();

    @Select("SELECT COUNT(*) FROM word_state")
    long countMarked();

    @Select("SELECT COUNT(*) FROM word_state WHERE state = 'known'")
    long countKnown();

    @Select("SELECT COUNT(*) FROM word_state WHERE state = 'unknown'")
    long countUnknown();
}