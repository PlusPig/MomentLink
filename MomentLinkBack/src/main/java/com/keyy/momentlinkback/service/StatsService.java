package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.dto.response.ContentResponse;
import com.keyy.momentlinkback.dto.response.StatsResponse;
import com.keyy.momentlinkback.entity.Content;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final CommentRepository commentRepository;
    private final LikeRecordRepository likeRecordRepository;
    private final RatingRepository ratingRepository;

    public StatsResponse getOverviewStats() {
        Long totalUsers = userRepository.countActiveUsers();
        Long activeUsers = userRepository.countNewUsers(
                LocalDateTime.now().minusDays(7));
        Long totalContents = contentRepository.countActiveContents();
        Long todayContents = contentRepository.countNewContents(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        Long totalComments = commentRepository.countActiveComments();
        Long totalLikes = likeRecordRepository.countAllLikes();
        Double avgRating = ratingRepository.calculateOverallAvgRating();

        return StatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalContents(totalContents)
                .todayContents(todayContents)
                .totalComments(totalComments)
                .totalLikes(totalLikes)
                .avgRating(avgRating != null ? avgRating : 0.0)
                .build();
    }

    public Object getUserGrowthStats(String startDate, String endDate) {
        // TODO: 实现用户增长趋势统计
        return null;
    }

    public Object getContentTrendStats(String startDate, String endDate) {
        // TODO: 实现内容发布趋势统计
        return null;
    }

    public Object getActiveUsersStats(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 按天统计活跃用户（发布内容、评论、点赞的用户）
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(days - 1 - i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            // 统计当天活跃用户（发布内容、评论、点赞的用户）
            Set<Long> activeUserIds = new HashSet<>();
            
            // 发布内容的用户
            List<Long> contentUserIds = contentRepository.findActiveUserIdsByDateRange(dayStart, dayEnd);
            activeUserIds.addAll(contentUserIds);
            
            // 发表评论的用户
            List<Long> commentUserIds = commentRepository.findActiveUserIdsByDateRange(dayStart, dayEnd);
            activeUserIds.addAll(commentUserIds);
            
            // 点赞的用户
            List<Long> likeUserIds = likeRecordRepository.findActiveUserIdsByDateRange(dayStart, dayEnd);
            activeUserIds.addAll(likeUserIds);
            
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayStats.put("day", date.format(DateTimeFormatter.ofPattern("MM/dd")));
            dayStats.put("count", activeUserIds.size());
            dayStats.put("activeUsers", activeUserIds.size());
            result.add(dayStats);
        }
        
        return result;
    }

    public Object getHotContents(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Content> hotContents = contentRepository.findHotContents(pageable);
        
        return hotContents.stream().map(content -> {
            User user = userRepository.findById(content.getUserId()).orElse(null);
            ContentResponse response = new ContentResponse();
            response.setId(content.getId());
            response.setUserId(content.getUserId());
            response.setType(content.getType());
            response.setContentText(content.getContentText());
            response.setMediaUrls(content.getMediaUrls());
            response.setTags(content.getTags() != null ? 
                    Arrays.asList(content.getTags().split(",")) : new ArrayList<>());
            response.setViewCount(content.getViewCount());
            response.setLikeCount(content.getLikeCount());
            response.setCommentCount(content.getCommentCount());
            response.setAvgRating(content.getAvgRating());
            response.setRatingCount(content.getRatingCount());
            response.setCreatedAt(content.getCreatedAt());
            
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setNickname(user.getNickname());
                response.setAvatar(user.getAvatar());
            }
            
            return response;
        }).collect(Collectors.toList());
    }
}
