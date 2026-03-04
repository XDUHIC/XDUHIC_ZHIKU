package com.example.backend.service;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Article;

public interface ArticleService {
    PageResponse<Article> list(int page, int size, String search, String source);
    Article getById(Long id, boolean increaseViewCount);
    Article save(Article article);
}



