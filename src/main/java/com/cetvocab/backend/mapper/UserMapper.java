package com.cetvocab.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cetvocab.backend.model.UserRow;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password_hash, display_name, "
            + "is_guest AS guest, created_at "
            + "FROM `user` WHERE username = #{username}")
    UserRow findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password_hash, display_name, "
            + "is_guest AS guest, created_at "
            + "FROM `user` WHERE id = #{id}")
    UserRow findById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM `user` WHERE username = #{username}")
    long countByUsername(@Param("username") String username);

    @Insert("INSERT INTO `user` (username, password_hash, display_name, is_guest) "
            + "VALUES (#{username}, #{passwordHash}, #{displayName}, #{guest})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserRow row);

    @Update("UPDATE `user` SET password_hash = #{hash} WHERE id = #{id}")
    void updatePassword(@Param("id") Long id, @Param("hash") String hash);
}
