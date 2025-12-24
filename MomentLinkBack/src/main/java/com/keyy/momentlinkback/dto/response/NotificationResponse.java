package com.keyy.momentlinkback.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Integer type; // 1:好友申请 2:点赞 3:评论 4:系统通知
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long contentId;
    private Long relatedId; // 关联ID，用于存储 friendshipId 等
    private String message;
    private Integer status; // 0:未读 1:已读
    private Integer friendshipStatus; // 好友申请状态：0待接受,1已接受,2已拒绝,3已屏蔽（仅type=1时有效）
    private LocalDateTime createdAt;
}
