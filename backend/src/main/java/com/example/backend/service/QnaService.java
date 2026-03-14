package com.example.backend.service;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.QnaAnswer;
import com.example.backend.entity.QnaQuestion;
import java.util.Map;

public interface QnaService {
    PageResponse<QnaQuestion> listQuestions(int page, int size, String search, String tags, String targetType, Long targetId);
    QnaQuestion getQuestionById(Long id, boolean increaseView);
    QnaQuestion createQuestion(Long userId, String title, String content, String tags, String targetType, Long targetId);
    PageResponse<QnaAnswer> listAnswers(Long questionId, int page, int size);
    QnaAnswer createAnswer(Long userId, Long questionId, String content);
    QnaAnswer acceptAnswer(Long questionOwnerId, Long answerId);
    Map<String, Object> toggleLike(Long userId, Long answerId);
}
