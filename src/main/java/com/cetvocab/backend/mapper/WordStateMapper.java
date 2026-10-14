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
            + "lapses, next_review_at, last_reviewed_at FROM word_state WHERE word_id = #{wordId}")
    WordStateRow findByWordId(@Param("wordId") Long wordId);

    @Select("SELECT word_id FROM word_state "
            + "WHERE next_review_at <= NOW() AND word_id <> #{excludeId} "
            + "ORDER BY next_review_at ASC LIMIT 1")
    Long findMostOverdue(@Param("excludeId") Long excludeId);

    @Select("SELECT word_id FROM word_state "
            + "WHERE word_id <> #{excludeId} "
            + "ORDER BY next_review_at ASC LIMIT 1")
    Long findSoonest(@Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM word_state WHERE next_review_at <= NOW()")
    long countDue();

    @Select("SELECT COUNT(*) FROM word_state WHERE state = 'known'")
    long countKnown();

    @Select("SELECT COUNT(*) FROM word_state WHERE state = 'unknown'")
    long countUnknown();

    @Insert("INSERT INTO word_state (word_id, state, ease_factor, interval_minutes, "
            + "repetitions, lapses, next_review_at, last_reviewed_at) "
            + "VALUES (#{wordId}, #{state}, #{easeFactor}, #{intervalMinutes}, "
            + "#{repetitions}, #{lapses}, #{nextReviewAt}, #{lastReviewedAt}) "
            + "ON DUPLICATE KEY UPDATE state = VALUES(state), "
            + "ease_factor = VALUES(ease_factor), interval_minutes = VALUES(interval_minutes), "
            + "repetitions = VALUES(repetitions), lapses = VALUES(lapses), "
            + "next_review_at = VALUES(next_review_at), last_reviewed_at = VALUES(last_reviewed_at)")
    void upsert(WordStateRow row);
}