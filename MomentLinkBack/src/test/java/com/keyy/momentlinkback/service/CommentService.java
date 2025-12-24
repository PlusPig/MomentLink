package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.dto.request.CommentRequest;
import com.keyy.momentlinkback.dto.response.CommentResponse;
import com.keyy.momentlinkback.entity.Comment;
import com.keyy.momentlinkback.entity.Content;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.exception.ResourceNotFoundException;
import com.keyy.momentlinkback.exception.UnauthorizedException;
import com.keyy.momentlinkback.repository.CommentRepository;
import com.keyy.momentlinkback.repository.ContentRepository;
import com.keyy.momentlinkback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final NotificationService notificationService;
    private final SensitiveWordService sensitiveWordService;

    @Transactional
    public CommentResponse createComment(String username, CommentRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Content content = contentRepository.findById(request.getContentId())
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));

        // 敏感词过滤
        String filteredText = sensitiveWordService.filter(request.getCommentText());

        Comment comment = new Comment();
        comment.setContentId(request.getContentId());
        comment.setUserId(user.getId());
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setCommentText(filteredText);
        comment.setDeleted(0);

        comment = commentRepository.save(comment);

        // 更新内容的评论数
        content.setCommentCount(content.getCommentCount() + 1);
        contentRepository.save(content);

        // 发送通知
        if (comment.getParentId() == 0) {
            // 评论内容
            notificationService.createNotification(
                    content.getUserId(), 3, user.getId(),
                    content.getId(), user.getNickname() + " 评论了你的内容");
        } else if (comment.getReplyToUserId() != null) {
            // 回复评论
            notificationService.createNotification(
                    comment.getReplyToUserId(), 3, user.getId(),
                    content.getId(), user.getNickname() + " 回复了你的评论");
        }

        return convertToResponse(comment, user);
    }

    public PageResult<CommentResponse> getCommentsByContent(Long contentId,
                                                            int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 只查询一级评论
        Page<Comment> commentPage = commentRepository
                .findByContentIdAndParentIdAndDeleted(contentId, 0L, 0, pageable);

        List<CommentResponse> responses = commentPage.getContent().stream()
                .map(comment -> {
                    User user = userRepository.findById(comment.getUserId()).orElse(null);
                    CommentResponse response = convertToResponse(comment, user);

                    // 加载子评论
                    List<Comment> replies = commentRepository
                            .findByParentIdAndDeleted(comment.getId(), 0);
                    if (!replies.isEmpty()) {
                        response.setReplies(replies.stream()
                                .map(reply -> {
                                    User replyUser = userRepository.findById(reply.getUserId()).orElse(null);
                                    return convertToResponse(reply, replyUser);
                                })
                                .toList());
                    }

                    return response;
                })
                .toList();

        return PageResult.<CommentResponse>builder()
                .list(responses)
                .total(commentPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    private CommentResponse convertToResponse(Comment comment, User user) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContentId(comment.getContentId());
        response.setParentId(comment.getParentId());
        response.setReplyToUserId(comment.getReplyToUserId());
        response.setCommentText(comment.getCommentText());
        response.setLikeCount(comment.getLikeCount());
        response.setCreatedAt(comment.getCreatedAt());

        if (user != null) {
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setUserAvatar(user.getAvatar());
        }

        return response;
    }

    @Transactional
    public void deleteComment(String username, Long commentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在"));

        if (!comment.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("无权限删除此评论");
        }

        comment.setDeleted(1);
        commentRepository.save(comment);

        // 更新内容的评论数
        Content content = contentRepository.findById(comment.getContentId())
                .orElseThrow(() -> new ResourceNotFoundException("内容不存在"));
        content.setCommentCount(Math.max(0, content.getCommentCount() - 1));
        contentRepository.save(content);
    }

    @Transactional
    public void likeComment(String username, Long commentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在"));

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);

        // 发送通知
        notificationService.createNotification(
                comment.getUserId(), 4, user.getId(),
                comment.getContentId(), user.getNickname() + " 点赞了你的评论");
    }
}
