package com.example.backend.mapper;

import com.example.backend.entity.Share;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShareMapper {
    
    @Select({
        "<script>",
        "SELECT s.id, s.title, s.content, s.category, s.tags, s.author_id AS authorId, s.text_url AS textUrl, s.view_count AS viewCount, s.created_at AS createdAt,",
        "COALESCE(u.nickname, u.username) AS authorName",
        "FROM shares s LEFT JOIN users u ON s.author_id = u.id",
        "<where>",
        "<if test='category != null and category != \"\" and category != \"全部\"'>",
        "AND s.category = REPLACE(#{category}, '\"', '')",
        "</if>",
        "<if test='search != null and search != \"\"'>",
        "AND (s.title LIKE CONCAT('%', REPLACE(#{search}, '\"', ''), '%') OR s.content LIKE CONCAT('%', REPLACE(#{search}, '\"', ''), '%'))",
        "</if>",
        "</where>",
        "ORDER BY s.created_at DESC, s.id DESC",
        "LIMIT ${limit} OFFSET ${offset}",
        "</script>"
    })
    List<Share> list(@Param("offset") int offset, @Param("limit") int limit, @Param("search") String search, @Param("category") String category);
    
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM shares s",
        "<where>",
        "<if test='category != null and category != \"\" and category != \"全部\"'>",
        "AND s.category = REPLACE(#{category}, '\"', '')",
        "</if>",
        "<if test='search != null and search != \"\"'>",
        "AND (s.title LIKE CONCAT('%', REPLACE(#{search}, '\"', ''), '%') OR s.content LIKE CONCAT('%', REPLACE(#{search}, '\"', ''), '%'))",
        "</if>",
        "</where>",
        "</script>"
    })
    long count(@Param("search") String search, @Param("category") String category);
    
    @Select("SELECT s.id, s.title, s.content, s.category, s.tags, s.author_id AS authorId, s.text_url AS textUrl, s.view_count AS viewCount, s.created_at AS createdAt, " +
            "COALESCE(u.nickname, u.username) AS authorName FROM shares s LEFT JOIN users u ON s.author_id = u.id WHERE s.id = #{id}")
    Share getById(@Param("id") Long id);
    
    @Update("UPDATE shares SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
    
    @Insert("INSERT INTO shares (title, content, category, tags, author_id, text_url, view_count, created_at) VALUES (#{title}, #{content}, #{category}, #{tags}, #{authorId}, #{textUrl}, #{viewCount}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Share share);

    @Update("UPDATE shares SET text_url = #{textUrl} WHERE id = #{id}")
    void updateTextUrl(@Param("id") Long id, @Param("textUrl") String textUrl);

    // 测试方法
    @Select("SELECT id, title, content, category, tags, author_id AS authorId, text_url AS textUrl, view_count AS viewCount, created_at AS createdAt FROM shares")
    List<Share> testDirectQuery();
    
    @Select("SELECT id, title, content, category, tags, author_id AS authorId, text_url AS textUrl, view_count AS viewCount, created_at AS createdAt FROM shares WHERE category = #{category}")
    List<Share> testParameterQuery(@Param("category") String category);
    
    @Select("SELECT id, title, content, category, tags, author_id AS authorId, text_url AS textUrl, view_count AS viewCount, created_at AS createdAt FROM shares WHERE category = #{category} ORDER BY created_at DESC, id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Share> testParameterWithLimitQuery(@Param("offset") int offset, @Param("limit") int limit, @Param("category") String category);
}