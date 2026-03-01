package com.example.backend.service;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Share;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ShareService {
    PageResponse<Share> list(int page, int size, String search, String category);
    Share getById(Long id, boolean increaseViewCount);
    void incrementViewCount(Long id);
    Share create(Share share, Long authorId, MultipartFile documentFile);
    
    // 测试方法
    List<Share> testDirectQuery();
    List<Share> testParameterQuery(String category);
    List<Share> testParameterWithLimitQuery(int offset, int limit, String category);
}