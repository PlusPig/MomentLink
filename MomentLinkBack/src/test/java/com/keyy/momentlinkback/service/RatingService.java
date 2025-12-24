package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.entity.Content;
import com.keyy.momentlinkback.entity.Rating;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.exception.BadRequestException;
import com.keyy.momentlinkback.exception.ResourceNotFoundException;
import com.keyy.momentlinkback.repository.ContentRepository;
import com.keyy.momentlinkback.repository.RatingRepository;
import com.keyy.momentlinkback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    @Transactional
    public void rateContent(String username, Long contentId, int score) {
        if (score < 1 || score > 5) {
            throw new BadRequestException("评分必须在1-5之间");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        // 检查是否已评分
        Rating rating = ratingRepository
                .findByContentIdAndUserId(contentId, user.getId())
                .orElse(new Rating());

        rating.setContentId(contentId);
        rating.setUserId(user.getId());
        rating.setScore(score);
        ratingRepository.save(rating);

        // 重新计算平均分
        updateContentRating(contentId);
    }

    public Object getMyRatings(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // TODO: 实现分页查询用户的所有评分
        return PageResult.builder()
                .list(null)
                .total(0L)
                .page(page)
                .size(size)
                .build();
    }

    public Object getContentRatings(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("avgRating", content.getAvgRating());
        result.put("ratingCount", content.getRatingCount());

        // 统计各星级数量
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        // TODO: 查询实际分布

        result.put("distribution", distribution);
        return result;
    }

    @Transactional
    public void deleteRating(String username, Long contentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Rating rating = ratingRepository
                .findByContentIdAndUserId(contentId, user.getId())
                .orElseThrow(() -> new BadRequestException("未找到评分记录"));

        ratingRepository.delete(rating);

        // 重新计算平均分
        updateContentRating(contentId);
    }

    private void updateContentRating(Long contentId) {
        Double avgRating = ratingRepository.calculateAvgRating(contentId);
        int ratingCount = ratingRepository.countByContentId(contentId);

        Content content = contentRepository.findById(contentId).orElse(null);
        if (content != null) {
            content.setAvgRating(avgRating != null ? avgRating : 0.0);
            content.setRatingCount(ratingCount);
            contentRepository.save(content);
        }
    }
}
