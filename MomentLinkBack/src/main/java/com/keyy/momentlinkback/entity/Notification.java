// Notification.java
package com.keyy.momentlinkback.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer type; // 1:好友申请 2:点赞 3:评论 4:系统通知

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "related_id")
    private Long relatedId; // 关联ID，用于存储 friendshipId 等

    @Column(length = 500)
    private String message;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status = 0; // 0:未读 1:已读

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}