package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.QnaAnswer;
import com.example.backend.entity.QnaQuestion;
import com.example.backend.entity.User;
import com.example.backend.service.QnaService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 答疑解惑模块：问题与回答
 */
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final UserService userService;

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<PageResponse<QnaQuestion>>> listQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tags) {
        PageResponse<QnaQuestion> pr = qnaService.listQuestions(page, size, search, tags);
        return ResponseEntity.ok(ApiResponse.success(pr));
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<?> getQuestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean increaseView) {
        QnaQuestion q = qnaService.getQuestionById(id, increaseView);
        if (q == null) return ResponseEntity.ok(ApiResponse.error("问题不存在"));
        return ResponseEntity.ok(ApiResponse.success(q));
    }

    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "请先登录"));
        }
        User u = userService.getByUsername(authentication.getName());
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在"));
        }
        String title = body != null && body.get("title") != null ? body.get("title").toString() : "";
        String content = body != null && body.get("content") != null ? body.get("content").toString() : "";
        String tags = body != null && body.get("tags") != null ? body.get("tags").toString() : null;
        if (title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "标题不能为空"));
        }
        try {
            QnaQuestion created = qnaService.createQuestion(u.getId(), title, content, tags);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("发布问题失败: " + e.getMessage()));
        }
    }

    @GetMapping("/questions/{id}/answers")
    public ResponseEntity<ApiResponse<PageResponse<QnaAnswer>>> listAnswers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<QnaAnswer> pr = qnaService.listAnswers(id, page, size);
        return ResponseEntity.ok(ApiResponse.success(pr));
    }

    @PostMapping("/questions/{id}/answers")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "请先登录"));
        }
        User u = userService.getByUsername(authentication.getName());
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在"));
        }
        String content = body != null && body.get("content") != null ? body.get("content").toString() : "";
        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "回答内容不能为空"));
        }
        try {
            QnaAnswer created = qnaService.createAnswer(u.getId(), id, content);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("发布回答失败: " + e.getMessage()));
        }
    }

    @PostMapping("/answers/{id}/accept")
    public ResponseEntity<?> acceptAnswer(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "请先登录"));
        }
        User u = userService.getByUsername(authentication.getName());
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在"));
        }
        try {
            QnaAnswer ans = qnaService.acceptAnswer(u.getId(), id);
            return ResponseEntity.ok(ApiResponse.success(ans));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        }
    }

    @PostMapping("/answers/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "请先登录"));
        }
        User u = userService.getByUsername(authentication.getName());
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在"));
        }
        try {
            Map<String, Object> result = qnaService.toggleLike(u.getId(), id);
            return ResponseEntity.ok(ApiResponse.success("操作成功", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/questions/{id}/view")
    public ResponseEntity<?> incrementView(@PathVariable Long id) {
        qnaService.getQuestionById(id, true);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
