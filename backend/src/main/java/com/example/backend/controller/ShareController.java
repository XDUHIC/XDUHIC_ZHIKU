package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Share;
import com.example.backend.entity.User;
import com.example.backend.service.ShareService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/senior-shares")
public class ShareController {

    private final ShareService shareService;
    private final UserService userService;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    public ShareController(ShareService shareService, UserService userService) {
        this.shareService = shareService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Share>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        PageResponse<Share> pr = shareService.list(page, size, search, category);
        return ResponseEntity.ok(ApiResponse.success(pr));
    }

    @GetMapping("/test-direct")
    public ResponseEntity<ApiResponse<List<Share>>> testDirect() {
        List<Share> shares = shareService.testDirectQuery();
        return ResponseEntity.ok(ApiResponse.success(shares));
    }

    @GetMapping("/test-param")
    public ResponseEntity<ApiResponse<List<Share>>> testParam(@RequestParam String category) {
        List<Share> shares = shareService.testParameterQuery(category);
        return ResponseEntity.ok(ApiResponse.success(shares));
    }

    @GetMapping("/test-param-limit")
    public ResponseEntity<ApiResponse<List<Share>>> testParamLimit(@RequestParam String category) {
        List<Share> shares = shareService.testParameterWithLimitQuery(0, 9, category);
        return ResponseEntity.ok(ApiResponse.success(shares));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Share>> get(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "true") boolean increaseViewCount) {
        Share share = shareService.getById(id, increaseViewCount);
        if (share == null) {
            return ResponseEntity.ok(ApiResponse.error("分享不存在"));
        }
        if (share.getTextUrl() == null || share.getTextUrl().isBlank()) {
            Path mdPath = Paths.get(uploadPath, "shares", "contents", id + ".md");
            if (Files.exists(mdPath)) {
                share.setTextUrl("/uploads/shares/contents/" + id + ".md");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(share));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> addView(@PathVariable Long id) {
        shareService.getById(id, true);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Share>> create(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "category", required = false, defaultValue = "others") String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "documentFile", required = false) MultipartFile documentFile,
            Authentication authentication) {
        try {
            Long authorId = null;
            if (authentication != null) {
                User u = userService.getByUsername(authentication.getName());
                if (u != null) authorId = u.getId();
            }
            Share share = new Share();
            share.setTitle(title);
            share.setContent(content);
            share.setCategory(category);
            share.setTags(tags);
            Share createdShare = shareService.create(share, authorId, documentFile);
            return ResponseEntity.ok(ApiResponse.success(createdShare));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("创建分享失败: " + e.getMessage()));
        }
    }
}


