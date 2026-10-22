package com.cetvocab.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MasteryMapper {

    @Update("UPDATE word_state SET lapses = 0, interval_minutes = 525600, "
            + "repetitions = 10, ease_factor = 2.8, "
            + "next_review_at = DATE_ADD(NOW(), INTERVAL 365 DAY), "
            + "last_reviewed_at = NOW() "
            + "WHERE user_id = #{userId} AND word_id = #{wordId}")
    void markMastered(@Param("userId") Long userId, @Param("wordId") Long wordId);
}