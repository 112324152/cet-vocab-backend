package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.WordStateRow;

@Mapper
public interface WordStateMapper {

    @Select("SELECT word_id, state, ease_factor, interval_minutes, repetitions, "
            + "lapses, next_review_at, last_reviewed_at FROM word_state "
            + "WHERE user_id = #{userId} AND word_id = #{wordId}")
    WordStateRow findByWordId(@Param("userId") Long userId, @Param("wordId") Long wordId);

    @Select("SELECT word_id FROM word_state "
            + "WHERE user_id = #{userId} AND next_review_at <= NOW() AND word_id <> #{excludeId} "
            + "ORDER BY next_review_at ASC LIMIT 1")
    Long findMostOverdue(@Param("userId") Long userId, @Param("excludeId") Long excludeId);

    @Select("SELECT word_id FROM word_state "
            + "WHERE user_id = #{userId} AND word_id <> #{excludeId} "
            + "ORDER BY next_review_at ASC LIMIT 1")
    Long findSoonest(@Param("userId") Long userId, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM word_state "
            + "WHERE user_id = #{userId} AND next_review_at <= NOW()")
    long countDue(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM word_state "
            + "WHERE user_id = #{userId} AND state = 'known'")
    long countKnown(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM word_state "
            + "WHERE user_id = #{userId} AND state = 'unknown'")
    long countUnknown(@Param("userId") Long userId);

    @Insert("INSERT INTO word_state (user_id, word_id, state, ease_factor, interval_minutes, "
            + "repetitions, lapses, next_review_at, last_reviewed_at) "
            + "VALUES (#{userId}, #{wordId}, #{state}, #{easeFactor}, #{intervalMinutes}, "
            + "#{repetitions}, #{lapses}, #{nextReviewAt}, #{lastReviewedAt}) "
            + "ON DUPLICATE KEY UPDATE state = VALUES(state), "
            + "ease_factor = VALUES(ease_factor), interval_minutes = VALUES(interval_minutes), "
            + "repetitions = VALUES(repetitions), lapses = VALUES(lapses), "
            + "next_review_at = VALUES(next_review_at), last_reviewed_at = VALUES(last_reviewed_at)")
    void upsert(WordStateRow row);

    @Select("SELECT word_id, state, ease_factor, interval_minutes, repetitions, "
            + "lapses, next_review_at, last_reviewed_at FROM word_state "
            + "WHERE user_id = #{userId} AND lapses > 0 "
            + "ORDER BY lapses DESC, ease_factor ASC, interval_minutes ASC "
            + "LIMIT #{limit}")
    List<WordStateRow> findHardest(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM word_state "
            + "WHERE user_id = #{userId} "
            + "AND interval_minutes >= #{min} AND interval_minutes < #{max}")
    long countByIntervalRange(@Param("userId") Long userId,
                              @Param("min") long min,
                              @Param("max") long max);
}
