package com.example.backend.service.impl;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.QnaAnswer;
import com.example.backend.entity.QnaQuestion;
import com.example.backend.mapper.PointMapper;
import com.example.backend.mapper.QnaAnswerMapper;
import com.example.backend.mapper.QnaQuestionMapper;
import com.example.backend.service.PointService;
import com.example.backend.service.QnaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QnaServiceImpl implements QnaService {

    private final QnaQuestionMapper questionMapper;
    private final QnaAnswerMapper answerMapper;
    private final PointService pointService;
    private final PointMapper pointMapper;

    @Value("${app.qna.points.accepted:10}")
    private int pointsAccepted;
    @Value("${app.qna.points.like-threshold:5}")
    private int likeThreshold;
    @Value("${app.qna.points.like-reward:2}")
    private int likeReward;

    public QnaServiceImpl(QnaQuestionMapper questionMapper, QnaAnswerMapper answerMapper,
                          PointService pointService, PointMapper pointMapper) {
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
        this.pointService = pointService;
        this.pointMapper = pointMapper;
    }

    @Override
    public PageResponse<QnaQuestion> listQuestions(int page, int size, String search, String tags, String targetType, Long targetId) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        List<QnaQuestion> data = questionMapper.list(offset, size, search, tags, targetType, targetId);
        long total = questionMapper.count(search, tags, targetType, targetId);
        return new PageResponse<>(data, total, page, size);
    }

    @Override
    public QnaQuestion getQuestionById(Long id, boolean increaseView) {
        if (increaseView) questionMapper.incrementViewCount(id);
        return questionMapper.getById(id);
    }

    @Override
    @Transactional
    public QnaQuestion createQuestion (Long userId, String title, String content, String tags, String
    targetType, Long targetId){
        QnaQuestion q = new QnaQuestion();
        q.setUserId(userId);
        q.setTitle(title);
        q.setContent(content);
        q.setTags(tags);
        q.setViewCount(0);
        q.setAnswerCount(0);
        q.setCreatedAt(LocalDateTime.now());
        q.setTargetType(targetType);
        q.setTargetId(targetId);
        questionMapper.insert(q);
        return questionMapper.getById(q.getId());
    }


    @Override
    public PageResponse<QnaAnswer> listAnswers(Long questionId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        List<QnaAnswer> all = answerMapper.listByQuestionId(questionId);
        long total = all.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, all.size());
        List<QnaAnswer> data = offset < all.size() ? all.subList(offset, end) : List.of();
        return new PageResponse<>(data, total, page, size);
    }

    @Override
    @Transactional
    public QnaAnswer createAnswer(Long userId, Long questionId, String content) {
        QnaAnswer a = new QnaAnswer();
        a.setQuestionId(questionId);
        a.setUserId(userId);
        a.setContent(content);
        a.setLikeCount(0);
        a.setIsAccepted(0);
        a.setCreatedAt(LocalDateTime.now());
        answerMapper.insert(a);
        questionMapper.incrementAnswerCount(questionId);
        return answerMapper.getById(a.getId());
    }

    @Override
    @Transactional
    public QnaAnswer acceptAnswer(Long questionOwnerId, Long answerId) {
        QnaAnswer ans = answerMapper.getById(answerId);
        if (ans == null) throw new RuntimeException("回答不存在");
        QnaQuestion q = questionMapper.getById(ans.getQuestionId());
        if (q == null) throw new RuntimeException("问题不存在");
        if (!q.getUserId().equals(questionOwnerId)) throw new RuntimeException("只有提问者可以采纳回答");
        if (ans.getIsAccepted() != null && ans.getIsAccepted() == 1) return ans;

        answerMapper.clearAcceptedByQuestionId(ans.getQuestionId());
        answerMapper.setAccepted(answerId);
        questionMapper.setAcceptedAnswer(ans.getQuestionId(), answerId);

        pointService.addPoints(ans.getUserId(), pointsAccepted, "回答被采纳", "qna_accepted", answerId);
        return answerMapper.getById(answerId);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long answerId) {
        QnaAnswer ans = answerMapper.getById(answerId);
        if (ans == null) throw new RuntimeException("回答不存在");
        int hasLiked = answerMapper.hasLiked(answerId, userId);
        boolean liked;
        if (hasLiked > 0) {
            answerMapper.removeLike(answerId, userId);
            answerMapper.decrementLikeCount(answerId);
            liked = false;
        } else {
            answerMapper.addLike(answerId, userId);
            answerMapper.incrementLikeCount(answerId);
            liked = true;
            int newCount = ans.getLikeCount() == null ? 1 : ans.getLikeCount() + 1;
            if (newCount >= likeThreshold && pointMapper.countByRef("qna_like_reward", answerId) == 0) {
                pointService.addPoints(ans.getUserId(), likeReward, "回答获赞达阈值", "qna_like_reward", answerId);
            }
        }
        QnaAnswer updated = answerMapper.getById(answerId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", updated.getLikeCount());
        return result;
    }
}
