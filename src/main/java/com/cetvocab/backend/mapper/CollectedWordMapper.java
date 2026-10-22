package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.CollectedWordRow;

@Mapper
public interface CollectedWordMapper {

    @Select("SELECT id, user_id, word, phonetic, translation, context, collected_at "
            + "FROM collected_word WHERE user_id = #{userId} "
            + "ORDER BY collected_at DESC")
    List<CollectedWordRow> findAll(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM collected_word WHERE user_id = #{userId}")
    long count(@Param("userId") Long userId);

    @Insert("INSERT INTO collected_word (user_id, word, phonetic, translation, context) "
            + "VALUES (#{userId}, #{word}, #{phonetic}, #{translation}, #{context}) "
            + "ON DUPLICATE KEY UPDATE phonetic = VALUES(phonetic), "
            + "translation = VALUES(translation), context = VALUES(context), "
            + "collected_at = CURRENT_TIMESTAMP")
    void upsert(CollectedWordRow row);

    @Delete("DELETE FROM collected_word WHERE user_id = #{userId} AND id = #{id}")
    void deleteById(@Param("userId") Long userId, @Param("id") Long id);
}