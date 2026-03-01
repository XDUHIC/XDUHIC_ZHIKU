package com.example.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.backend.entity.User;

@Mapper
public interface UserMapper {


    @Insert("INSERT INTO users(username, password, nickname, status,student_id,college, created_at) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{status},#{studentId},#{college}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT id, username, password, nickname, email, avatar_url, student_id,college, bio, status, hic, last_login_time, created_at " +
            "FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT id, username, password, nickname, email, avatar_url,student_id, college, bio, status, hic, last_login_time, created_at " +
            "FROM users WHERE id = #{id}")
    User findById(Long id);

    @Update({
            "<script>",
            "UPDATE users",
            "<set>",
            "  <if test='nickname != null'>nickname = #{nickname},</if>",
            "  <if test='email != null'>email = #{email},</if>",
            "  <if test='avatarUrl != null'>avatar_url = #{avatarUrl},</if>",
            "  <if test='college != null'>college = #{college},</if>",
            "  <if test='bio != null'>bio = #{bio},</if>",
            "  <if test='hic != null'>hic = #{hic},</if>",
            "  <if test='studentId != null'>student_id = #{studentId},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateProfile(User user);

//    @Insert("INSERT INTO users(username, password, nickname, status, created_at) " +
//            "VALUES(#{username}, #{password}, #{nickname}, #{status}, NOW())")
//    @Options(useGeneratedKeys = true, keyProperty = "id")
//    int insert(User user);
//
//    @Select("SELECT id, username, password, nickname, email, avatar_url, college, bio, status, hic, last_login_time, created_at " +
//            "FROM users WHERE username = #{username}")
//    User findByUsername(String username);
//
//    @Select("SELECT id, username, password, nickname, email, avatar_url, college, bio, status, hic, last_login_time, created_at " +
//            "FROM users WHERE id = #{id}")
//    User findById(Long id);

//    @Update({
//            "<script>",
//            "UPDATE users",
//            "<set>",
//            "  <if test='nickname != null'>nickname = #{nickname},</if>",
//            "  <if test='email != null'>email = #{email},</if>",
//            "  <if test='avatarUrl != null'>avatar_url = #{avatarUrl},</if>",
//            "  <if test='college != null'>college = #{college},</if>",
//            "  <if test='bio != null'>bio = #{bio},</if>",
//            "  <if test='hic != null'>hic = #{hic},</if>",
//            "</set>",
//            "WHERE id = #{id}",
//            "</script>"
//    })
//    int updateProfile(User user);

    @Update("UPDATE users SET password = REPLACE(#{password}, '\"', '') WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE users SET avatar_url = #{avatarUrl} WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);
    
    // 备用方法 - 使用实体对象
    @Update("UPDATE users SET avatar_url = #{avatarUrl} WHERE id = #{id}")
    int updateAvatarByEntity(User user);

    @Update("UPDATE users SET last_login_time = NOW() WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Long id);

    @Update("UPDATE users SET hic = #{hic} WHERE id = #{id}")
    int updateHicStatus(@Param("id") Long id, @Param("hic") Integer hic);

    @Select("SELECT id, username, password, nickname, email, avatar_url, college, bio, status, hic, last_login_time, created_at " +
            "FROM users ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<User> list(@Param("offset") int offset, @Param("limit") int limit);

//    boolean findByStudentId(String studentId);
    @Select("SELECT id, username, password, nickname, email, avatar_url, college, bio, status, hic, last_login_time, created_at " +
            "FROM users WHERE student_id = #{studentId}")
    User findByStudentId(@Param("studentId") String studentId);
}


