package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.ChatMessageRow;

@Mapper
public interface ChatMessageMapper {

    @Insert("INSERT INTO chat_message (user_id, word_id, role, content) "
            + "VALUES (#{userId}, #{wordId}, #{role}, #{content})")
    void insert(@Param("userId") Long userId,
                @Param("wordId") Long wordId,
                @Param("role") String role,
                @Param("content") String content);

    @Select("SELECT id, user_id, word_id, role, content, created_at FROM ("
            + "  SELECT id, user_id, word_id, role, content, created_at "
            + "  FROM chat_message "
            + "  WHERE user_id = #{userId} AND word_id = #{wordId} "
            + "  ORDER BY id DESC LIMIT #{limit}"
            + ") t ORDER BY id ASC")
    List<ChatMessageRow> findRecent(@Param("userId") Long userId,
                                    @Param("wordId") Long wordId,
                                    @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM chat_message WHERE user_id = #{userId} AND word_id = #{wordId}")
    long countByWord(@Param("userId") Long userId, @Param("wordId") Long wordId);
}