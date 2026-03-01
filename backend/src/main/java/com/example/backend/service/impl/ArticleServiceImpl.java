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
}


