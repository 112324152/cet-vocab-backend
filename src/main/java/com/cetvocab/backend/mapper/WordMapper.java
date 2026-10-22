package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.WordRow;

@Mapper
public interface WordMapper {

    @Select("SELECT id, text, phonetic, meanings, example, level FROM word ORDER BY id")
    List<WordRow> findAll();

    @Select("SELECT id, text, phonetic, meanings, example, level FROM word WHERE id = #{id}")
    WordRow findById(@Param("id") Long id);

    @Select("SELECT w.id FROM word w "
            + "LEFT JOIN word_state ws ON ws.word_id = w.id AND ws.user_id = #{userId} "
            + "WHERE ws.word_id IS NULL AND w.id <> #{excludeId} "
            + "ORDER BY RAND() LIMIT 1")
    Long findRandomNew(@Param("userId") Long userId, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM word")
    long countTotal();
}