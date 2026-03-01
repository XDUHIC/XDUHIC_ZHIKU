package com.example.backend.service;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Comment;

public interface CommentService {
    PageResponse<Comment> listByResourceId(Long resourceId, int page, int size);
    Comment add(Long resourceId, Long userId, Long parentId, String content);
}
