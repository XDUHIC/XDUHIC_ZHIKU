package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 为 .md 文件设置 Content-Type: text/markdown，使浏览器内联显示而非下载。
 * 优先于静态资源配置处理 /uploads/resources/contents/*.md 与 /uploads/shares/contents/*.md 请求。
 */
@RestController
public class MarkdownContentController {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping(value = "/uploads/resources/contents/{filename:.+\\.md}", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public ResponseEntity<Resource> serveResourceMarkdown(@PathVariable String filename) {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve("resources").resolve("contents").resolve(filename);
        return serveFile(filePath);
    }

    @GetMapping(value = "/uploads/shares/contents/{filename:.+\\.md}", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public ResponseEntity<Resource> serveShareMarkdown(@PathVariable String filename) {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve("shares").resolve("contents").resolve(filename);
        return serveFile(filePath);
    }

    private ResponseEntity<Resource> serveFile(Path filePath) {
        Resource resource = new FileSystemResource(filePath.toFile());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(resource);
    }
}
