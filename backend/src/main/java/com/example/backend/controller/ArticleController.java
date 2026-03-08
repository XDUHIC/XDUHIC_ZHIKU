package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Article;
import com.example.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Article>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String source) {
        PageResponse<Article> response = articleService.list(page, size, search, source);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Article>> get(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean increaseViewCount) {
        Article article = articleService.getById(id, increaseViewCount);
        if (article == null) {
            return ResponseEntity.ok(ApiResponse.error("文章不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> addView(@PathVariable Long id) {
        articleService.getById(id, true);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Article>> create(@RequestBody Article article) {
        try {
            System.out.println("Received article: " + article);
            Article savedArticle = articleService.save(article);
            return ResponseEntity.ok(ApiResponse.success(savedArticle));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("保存失败: " + e.getMessage()));
        }
    }
}


