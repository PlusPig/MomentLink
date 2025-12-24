package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.dto.request.ContentCreateRequest;
import com.keyy.momentlinkback.dto.request.ContentUpdateRequest;
import com.keyy.momentlinkback.dto.response.ContentDetailResponse;
import com.keyy.momentlinkback.dto.response.ContentResponse;
import com.keyy.momentlinkback.entity.Content;
import com.keyy.momentlinkback.entity.LikeRecord;
import com.keyy.momentlinkback.entity.Rating;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.exception.BadRequestException;
import com.keyy.momentlinkback.exception.ResourceNotFoundException;
import com.keyy.momentlinkback.exception.UnauthorizedException;
import com.keyy.momentlinkback.repository.ContentRepository;
import com.keyy.momentlinkback.repository.LikeRecordRepository;
import com.keyy.momentlinkback.repository.RatingRepository;
import com.keyy.momentlinkback.repository.UserRepository;
import com.keyy.momentlinkback.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final LikeRecordRepository likeRecordRepository;
    private final RatingRepository ratingRepository;
    private final FileStorageService fileStorageService;
    private final SensitiveWordService sensitiveWordService;
    private final VideoProcessingService videoProcessingService;
    private final NotificationService notificationService;

    @Transactional
    public ContentResponse createContent(String username,
                                         ContentCreateRequest request, List<MultipartFile> files) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 敏感词过滤
        if (request.getContentText() != null) {
            String filteredText = sensitiveWordService.filter(request.getContentText());
            request.setContentText(filteredText);
        }

        Content content = new Content();
        content.setUserId(user.getId());
        content.setType(request.getType());
        content.setContentText(request.getContentText());
        content.setTags(request.getTags());
        content.setStatus(1);
        content.setDeleted(0);

        // 处理媒体文件
        if (files != null && !files.isEmpty()) {
            List<String> mediaUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                String folder = request.getType() == 1 ? "images" : "videos";
                String url = fileStorageService.storeFile(file, folder);

                // 如果是视频,进行转码处理
                if (request.getType() == 2) {
                    url = videoProcessingService.processVideo(url);
                }

                mediaUrls.add(url);
            }
            content.setMediaUrls(mediaUrls);
        }

        content = contentRepository.save(content);
        return convertToResponse(content, user, null);
    }

    public PageResult<ContentResponse> getContents(int page, int size,
                                                   Integer type, String tags) {
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Content> contentPage;
        if (type != null) {
            contentPage = contentRepository.findByTypeAndStatus(type, 1, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            contentPage = contentRepository.findByTagsContainingAndStatus(
                    tags, 1, pageable);
        } else {
            contentPage = contentRepository.findByStatus(1, pageable);
        }

        List<ContentResponse> responses = contentPage.getContent().stream()
                .map(content -> {
                    User user = userRepository.findById(content.getUserId()).orElse(null);
                    return convertToResponse(content, user, null);
                })
                .toList();

        return PageResult.<ContentResponse>builder()
                .list(responses)
                .total(contentPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    public PageResult<ContentResponse> getContents(String username, int page, int size,
                                                   Integer type, String tags) {
        if (username == null || username.isBlank()) {
            return getContents(page, size, type, tags);
        }

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        List<Long> excludedUserIds = friendshipRepository.findBlockedPeerUserIds(currentUser.getId());
        if (excludedUserIds == null || excludedUserIds.isEmpty()) {
            return getContents(page, size, type, tags);
        }

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Content> contentPage;
        if (type != null) {
            contentPage = contentRepository.findByTypeAndStatusExcludingUsers(type, excludedUserIds, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            contentPage = contentRepository.findByTagsContainingAndStatusExcludingUsers(tags, excludedUserIds, pageable);
        } else {
            contentPage = contentRepository.findByStatusExcludingUsers(excludedUserIds, pageable);
        }

        List<ContentResponse> responses = contentPage.getContent().stream()
                .map(content -> {
                    User user = userRepository.findById(content.getUserId()).orElse(null);
                    return convertToResponse(content, user, null);
                })
                .toList();

        return PageResult.<ContentResponse>builder()
                .list(responses)
                .total(contentPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional
    public ContentDetailResponse getContentDetail(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        // 增加浏览次数
        content.setViewCount(content.getViewCount() + 1);
        contentRepository.save(content);

        User user = userRepository.findById(content.getUserId()).orElse(null);

        ContentDetailResponse response = new ContentDetailResponse();
        ContentResponse baseResponse = convertToResponse(content, user, null);
        org.springframework.beans.BeanUtils.copyProperties(baseResponse, response);

        // TODO: 加载评论列表

        return response;
    }

    @Transactional
    public ContentResponse updateContent(String username, Long id,
                                         ContentUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        if (!content.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("无权限修改此内容");
        }

        // 敏感词过滤
        if (request.getContentText() != null) {
            String filteredText = sensitiveWordService.filter(request.getContentText());
            content.setContentText(filteredText);
        }

        if (request.getTags() != null) {
            content.setTags(request.getTags());
        }

        content = contentRepository.save(content);
        return convertToResponse(content, user, null);
    }

    @Transactional
    public void deleteContent(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        if (!content.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("无权限删除此内容");
        }

        content.setDeleted(1);
        contentRepository.save(content);
    }

    public PageResult<ContentResponse> getMyContents(String username, int page,
                                                     int size, String startDate, String endDate, String tags) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Content> contentPage = contentRepository.findByUserId(
                user.getId(), pageable);

        // TODO: 添加日期和标签筛选

        List<ContentResponse> responses = contentPage.getContent().stream()
                .map(content -> convertToResponse(content, user, null))
                .toList();

        return PageResult.<ContentResponse>builder()
                .list(responses)
                .total(contentPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional
    public void likeContent(String username, Long contentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        // 检查是否已点赞
        if (likeRecordRepository.existsByUserIdAndContentId(user.getId(), contentId)) {
            throw new BadRequestException("已经点赞过了");
        }

        // 创建点赞记录
        LikeRecord record = new LikeRecord();
        record.setUserId(user.getId());
        record.setContentId(contentId);
        likeRecordRepository.save(record);

        // 更新点赞数
        content.setLikeCount(content.getLikeCount() + 1);
        contentRepository.save(content);

        // 发送通知
        notificationService.createNotification(content.getUserId(), 2,
                user.getId(), contentId, user.getNickname() + " 赞了你的内容");
    }

    @Transactional
    public void unlikeContent(String username, Long contentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        LikeRecord record = likeRecordRepository
                .findByUserIdAndContentId(user.getId(), contentId)
                .orElseThrow(() -> new BadRequestException("未点赞"));

        likeRecordRepository.delete(record);

        // 更新点赞数
        content.setLikeCount(Math.max(0, content.getLikeCount() - 1));
        contentRepository.save(content);
    }

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

        boolean isNew = rating.getId() == null;
        rating.setContentId(contentId);
        rating.setUserId(user.getId());
        rating.setScore(score);
        ratingRepository.save(rating);

        // 重新计算平均分
        Double avgRating = ratingRepository.calculateAvgRating(contentId);
        int ratingCount = ratingRepository.countByContentId(contentId);

        content.setAvgRating(avgRating != null ? avgRating : 0.0);
        content.setRatingCount(ratingCount);
        contentRepository.save(content);
    }

    @Transactional
    public PageResult<ContentResponse> getAllContents(int page, int size,
                                                      Long userId, Integer type, Integer status) {
        // 管理员查询所有内容（排除已删除的内容）
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Content> contentPage;
        Integer deleted = 0; // 排除已删除的内容
        
        // 构建查询条件
        if (userId != null && type != null && status != null) {
            // 三个条件都有
            contentPage = contentRepository.findByUserIdAndTypeAndStatusAndDeleted(
                    userId, type, status, deleted, pageable);
        } else if (userId != null && type != null) {
            // userId 和 type
            contentPage = contentRepository.findByUserIdAndTypeAndDeleted(
                    userId, type, deleted, pageable);
        } else if (userId != null && status != null) {
            // userId 和 status
            contentPage = contentRepository.findByUserIdAndStatusAndDeleted(
                    userId, status, deleted, pageable);
        } else if (type != null && status != null) {
            // type 和 status
            contentPage = contentRepository.findByTypeAndStatusAndDeleted(
                    type, status, deleted, pageable);
        } else if (userId != null) {
            // 只有 userId
            contentPage = contentRepository.findByUserIdAndDeleted(userId, deleted, pageable);
        } else if (type != null) {
            // 只有 type
            contentPage = contentRepository.findByTypeAndDeleted(type, deleted, pageable);
        } else if (status != null) {
            // 只有 status
            contentPage = contentRepository.findByStatusAndDeleted(status, deleted, pageable);
        } else {
            // 都没有，只过滤已删除的
            contentPage = contentRepository.findByDeleted(deleted, pageable);
        }

        List<ContentResponse> responses = contentPage.getContent().stream()
                .map(content -> {
                    User user = userRepository.findById(content.getUserId()).orElse(null);
                    return convertToResponse(content, user, null);
                })
                .toList();

        return PageResult.<ContentResponse>builder()
                .list(responses)
                .total(contentPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional
    public void deleteContentByAdmin(Long id, String reason) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        content.setDeleted(1);
        content.setStatus(2); // 违规
        contentRepository.save(content);

        // 发送违规通知
        String message = "您的内容因违规已被删除" +
                (reason != null ? "，原因：" + reason : "");
        notificationService.createNotification(content.getUserId(), 4,
                null, id, message);
    }

    @Transactional
    public void markAsViolation(Long id, String reason) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        content.setStatus(2);
        contentRepository.save(content);

        // 发送违规通知
        String message = "您的内容已被标记为违规" +
                (reason != null ? "，原因：" + reason : "");
        notificationService.createNotification(content.getUserId(), 4,
                null, id, message);
    }

    private ContentResponse convertToResponse(Content content, User user,
                                              String currentUsername) {
        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setUserId(content.getUserId());
        response.setType(content.getType());
        response.setContentText(content.getContentText());
        response.setMediaUrls(content.getMediaUrls());
        response.setTags(content.getTags() != null ?
                List.of(content.getTags().split(",")) : new ArrayList<>());
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

        // TODO: 判断当前用户是否点赞和评分

        return response;
    }
}
