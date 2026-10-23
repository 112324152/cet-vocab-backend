package com.cetvocab.backend.mapper;
import com.cetvocab.backend.model.DayCountDto;
import com.cetvocab.backend.model.ReviewLogRow;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.DailyStatDto;

@Mapper
public interface ReviewLogMapper {
    @Select("SELECT COUNT(*) FROM review_log "
            + "WHERE user_id = #{userId} AND DATE(reviewed_at) = CURDATE()")
    long countToday(@Param("userId") Long userId);

    @Select("SELECT DATE_FORMAT(reviewed_at, '%Y-%m-%d') AS date, COUNT(*) AS total "
            + "FROM review_log "
            + "WHERE user_id = #{userId} "
            + "AND reviewed_at >= #{start} AND reviewed_at < #{end} "
            + "GROUP BY DATE_FORMAT(reviewed_at, '%Y-%m-%d') "
            + "ORDER BY date")
    List<DayCountDto> countByDateRange(@Param("userId") Long userId,
                                       @Param("start") String start,
                                       @Param("end") String end);

    @Select("SELECT word_id, state, reviewed_at FROM review_log "
            + "WHERE user_id = #{userId} AND DATE(reviewed_at) = #{date} "
            + "ORDER BY reviewed_at ASC")
    List<ReviewLogRow> findByDate(@Param("userId") Long userId,
                                  @Param("date") String date);

    @Insert("INSERT INTO review_log (user_id, word_id, state) "
            + "VALUES (#{userId}, #{wordId}, #{state})")
    void insert(@Param("userId") Long userId,
                @Param("wordId") Long wordId,
                @Param("state") String state);

    @Select("SELECT COUNT(*) FROM review_log WHERE user_id = #{userId}")
    long countTotal(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM review_log WHERE user_id = #{userId} AND state = 'known'")
    long countKnown(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT DATE(reviewed_at)) FROM review_log WHERE user_id = #{userId}")
    long countStudyDays(@Param("userId") Long userId);

    @Select("SELECT DATE_FORMAT(reviewed_at, '%Y-%m-%d') AS date, "
            + "COUNT(*) AS total, "
            + "SUM(CASE WHEN state = 'known' THEN 1 ELSE 0 END) AS known_count "
            + "FROM review_log "
            + "WHERE user_id = #{userId} "
            + "AND reviewed_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) "
            + "GROUP BY DATE_FORMAT(reviewed_at, '%Y-%m-%d') "
            + "ORDER BY DATE_FORMAT(reviewed_at, '%Y-%m-%d')")
    List<DailyStatDto> findDaily(@Param("userId") Long userId, @Param("days") int days);
}
