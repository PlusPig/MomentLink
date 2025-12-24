package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.dto.response.NotificationResponse;
import com.keyy.momentlinkback.entity.Notification;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.exception.ResourceNotFoundException;
import com.keyy.momentlinkback.repository.FriendshipRepository;
import com.keyy.momentlinkback.repository.NotificationRepository;
import com.keyy.momentlinkback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public void createNotification(Long userId, Integer type,
                                   Long senderId, Long contentId, String message) {
        createNotification(userId, type, senderId, contentId, null, message);
    }

    @Transactional
    public void createNotification(Long userId, Integer type,
                                   Long senderId, Long contentId, Long relatedId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setSenderId(senderId);
        notification.setContentId(contentId);
        notification.setRelatedId(relatedId);
        notification.setMessage(message);
        notification.setStatus(0);

        notificationRepository.save(notification);
    }

    public PageResult<NotificationResponse> getNotifications(String username,
                                                             int page, int size, Integer type, Integer status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;
        if (type != null) {
            notificationPage = notificationRepository
                    .findByUserIdAndType(user.getId(), type, pageable);
        } else if (status != null) {
            notificationPage = notificationRepository
                    .findByUserIdAndStatus(user.getId(), status, pageable);
        } else {
            notificationPage = notificationRepository
                    .findByUserId(user.getId(), pageable);
        }

        return PageResult.<NotificationResponse>builder()
                .list(notificationPage.getContent().stream()
                        .map(this::convertToResponse).toList())
                .total(notificationPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    public Long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return notificationRepository.countUnreadByUserId(user.getId());
    }

    @Transactional
    public void markAsRead(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在"));

        if (!notification.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("通知不存在");
        }

        notification.setStatus(1);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    @Transactional
    public void deleteNotification(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在"));

        if (!notification.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("通知不存在");
        }

        notificationRepository.delete(notification);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setContentId(notification.getContentId());
        response.setRelatedId(notification.getRelatedId());
        response.setMessage(notification.getMessage());
        response.setStatus(notification.getStatus());
        response.setCreatedAt(notification.getCreatedAt());

        // 加载发送者信息
        if (notification.getSenderId() != null) {
            userRepository.findById(notification.getSenderId()).ifPresent(sender -> {
                response.setSenderId(sender.getId());
                response.setSenderName(sender.getNickname());
                response.setSenderAvatar(sender.getAvatar());
            });
        }

        // 如果是好友申请通知，加载好友关系状态
        if (notification.getType() == 1 && notification.getRelatedId() != null) {
            friendshipRepository.findById(notification.getRelatedId())
                    .ifPresent(friendship -> {
                        response.setFriendshipStatus(friendship.getStatus());
                    });
        }

        return response;
    }
}
