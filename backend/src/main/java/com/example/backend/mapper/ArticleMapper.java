package com.example.backend.mapper;

import com.example.backend.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {

    List<Article> list(@Param("offset") int offset, @Param("limit") int limit,
                      @Param("search") String search, @Param("source") String source);

    long count(@Param("search") String search, @Param("source") String source);

    @Select("SELECT id, title, summary, cover_image AS coverImage, view_count AS viewCount, link_url AS linkUrl, publish_time AS publishTime, created_at AS createdAt, source FROM articles WHERE id = #{id}")
    Article findById(@Param("id") Long id);

    @Update("UPDATE articles SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    // 测试方法：简化的搜索查询
    @Select("SELECT id, title, summary FROM articles WHERE title LIKE CONCAT('%', #{search}, '%') ORDER BY id LIMIT 5")
    List<Article> testSearch(@Param("search") String search);

    // 插入新文章
    @Insert("INSERT INTO articles (title, summary, cover_image, view_count, link_url, publish_time, source, created_at) " +
            "VALUES (#{title}, #{summary}, #{coverImage}, 0, #{linkUrl}, #{publishTime}, #{source}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);

    // 更新封面图片路径
    @Update("UPDATE articles SET cover_image = #{coverImage} WHERE id = #{id}")
    int updateCoverImage(@Param("id") Long id, @Param("coverImage") String coverImage);
}
