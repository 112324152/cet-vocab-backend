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
}