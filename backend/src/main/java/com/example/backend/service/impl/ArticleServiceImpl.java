package com.example.backend.service.impl;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Article;
import com.example.backend.mapper.ArticleMapper;
import com.example.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private ArticleMapper articleMapper;

    @Autowired
    public ArticleServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    /**
     * 去除参数首尾空格及首尾双引号（部分客户端会传 "undergrad" 导致与库中 undergrad 不匹配）。
     */
    private static String normalizeParam(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value.isEmpty() ? null : value;
    }

    @Override
    public PageResponse<Article> list(int page, int size, String search, String source) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        search = normalizeParam(search);
        source = normalizeParam(source);
        int offset = (page - 1) * size;
        if (search != null && !search.isEmpty()) {
            log.debug("Article search param: [{}], page={}, size={}, source={}", search, page, size, source);
        }
        List<Article> data = articleMapper.list(offset, size, search, source);
        long total = articleMapper.count(search, source);
        return new PageResponse<>(data, total, page, size);
    }

    @Override
    public Article getById(Long id, boolean increaseViewCount) {
        if (increaseViewCount) {
            articleMapper.incrementViewCount(id);
        }
        return articleMapper.findById(id);
    }

    @Override
    public Article save(Article article) {
        // 去除 coverImage 可能存在的首尾双引号
        if (article.getCoverImage() != null) {
            article.setCoverImage(normalizeParam(article.getCoverImage()));
        }

        // 保存文章，使用数据库自增ID
        articleMapper.insert(article);

        // 如果有封面图片路径，需要将其移动到标准路径
        if (article.getCoverImage() != null && !article.getCoverImage().isEmpty()) {
            moveCoverImageToStandardPath(article);
        }

        return article;
    }

    private void moveCoverImageToStandardPath(Article article) {
        try {
            String originalPath = article.getCoverImage();
            // 构建新的标准路径: /uploads/articles/images/{id}.jpg
            // 注意：现在 id 是手动设置的，文件命名使用文章 id
            String newFileName = article.getId() + ".jpg";
            String standardPath = "/uploads/articles/images/" + newFileName;
            log.info("moveCoverImageToStandardPath() - 文章ID: {}, 原路径: {}, 新标准路径: {}",
                    article.getId(), originalPath, standardPath);

            // 获取实际文件路径
            java.nio.file.Path sourcePath = java.nio.file.Paths.get(System.getProperty("user.dir"), originalPath.startsWith("/") ? originalPath.substring(1) : originalPath);
            java.nio.file.Path targetDir = java.nio.file.Paths.get(System.getProperty("user.dir"), "uploads/articles/images");
            java.nio.file.Path targetPath = targetDir.resolve(newFileName);

            // 确保目标目录存在
            if (!java.nio.file.Files.exists(targetDir)) {
                java.nio.file.Files.createDirectories(targetDir);
            }

            // 移动文件
            if (java.nio.file.Files.exists(sourcePath)) {
                java.nio.file.Files.move(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("封面图片已移动到: {}", targetPath);

                // 更新数据库中的路径
                article.setCoverImage(standardPath);
                log.debug("updateCoverImage() - 更新路径: [{}]", standardPath);
                articleMapper.updateCoverImage(article.getId(), standardPath);
            } else {
                log.warn("原始封面图片不存在: {}", sourcePath);
            }
        } catch (Exception e) {
            log.error("移动封面图片失败", e);
        }
    }
}


