package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.CollectedWordRow;

@Mapper
public interface CollectedWordMapper {

    @Select("SELECT id, word, phonetic, translation, context, collected_at "
            + "FROM collected_word ORDER BY collected_at DESC")
    List<CollectedWordRow> findAll();

    @Select("SELECT COUNT(*) FROM collected_word")
    long count();

    @Insert("INSERT INTO collected_word (word, phonetic, translation, context) "
            + "VALUES (#{word}, #{phonetic}, #{translation}, #{context}) "
            + "ON DUPLICATE KEY UPDATE context = VALUES(context), collected_at = CURRENT_TIMESTAMP")
    void upsert(CollectedWordRow row);
}