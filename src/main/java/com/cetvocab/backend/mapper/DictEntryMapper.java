package com.cetvocab.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cetvocab.backend.model.DictEntryRow;

@Mapper
public interface DictEntryMapper {

    @Select("SELECT COUNT(*) FROM dict_entry")
    long count();

    @Select("SELECT id, word, phonetic, translation, definition, tag "
            + "FROM dict_entry WHERE word = #{word} LIMIT 1")
    DictEntryRow findByWord(@Param("word") String word);

    @Select("<script>"
            + "SELECT id, word, phonetic, translation, definition, tag "
            + "FROM dict_entry WHERE word IN "
            + "<foreach collection='words' item='w' open='(' separator=',' close=')'>"
            + "#{w}"
            + "</foreach>"
            + "</script>")
    List<DictEntryRow> findByWords(@Param("words") List<String> words);
}