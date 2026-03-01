package com.example.backend.service.impl;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Share;
import com.example.backend.mapper.ShareMapper;
import com.example.backend.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ShareServiceImpl implements ShareService {

    private final ShareMapper shareMapper;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadPath;

    @Autowired
    public ShareServiceImpl(ShareMapper shareMapper) {
        this.shareMapper = shareMapper;
    }

    @Override
    public PageResponse<Share> list(int page, int size, String search, String category) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if ("全部".equals(category) || (category != null && category.trim().isEmpty())) category = null;
        int offset = (page - 1) * size;
        
        List<Share> data = shareMapper.list(offset, size, search, category);
        long total = shareMapper.count(search, category);
        
        return new PageResponse<>(data, total, page, size);
    }

    @Override
    public Share getById(Long id, boolean increaseViewCount) {
        if (increaseViewCount) {
            shareMapper.incrementViewCount(id);
        }
        return shareMapper.getById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        shareMapper.incrementViewCount(id);
    }

    @Override
    public List<Share> testDirectQuery() {
        return shareMapper.testDirectQuery();
    }

    @Override
    public List<Share> testParameterQuery(String category) {
        return shareMapper.testParameterQuery(category);
    }

    @Override
    public List<Share> testParameterWithLimitQuery(int offset, int limit, String category) {
        return shareMapper.testParameterWithLimitQuery(offset, limit, category);
    }

    @Override
    public Share create(Share share, Long authorId, MultipartFile documentFile) {
        if (share.getCreatedAt() == null) share.setCreatedAt(java.time.LocalDateTime.now());
        if (share.getViewCount() == null) share.setViewCount(0);
        if (authorId != null) share.setAuthorId(authorId);

        shareMapper.insert(share);
        Long id = share.getId();

        if (documentFile != null && !documentFile.isEmpty()) {
            try {
                String originalFilename = documentFile.getOriginalFilename();
                if (originalFilename != null && originalFilename.toLowerCase().endsWith(".md")) {
                    Path uploadDir = Paths.get(uploadPath, "shares", "contents");
                    Files.createDirectories(uploadDir);
                    Path filePath = uploadDir.resolve(id + ".md");
                    Files.copy(documentFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    String textUrl = "/uploads/shares/contents/" + id + ".md";
                    shareMapper.updateTextUrl(id, textUrl);
                    share.setTextUrl(textUrl);
                }
            } catch (IOException e) {
                throw new RuntimeException("保存 Markdown 文件失败: " + e.getMessage());
            }
        }
        return share;
    }
}