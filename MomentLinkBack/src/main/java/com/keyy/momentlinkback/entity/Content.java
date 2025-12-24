package com.keyy.momentlinkback.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "content")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer type; // 1:图片 2:视频 3:文本

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "media_urls", columnDefinition = "JSON")
    @Convert(converter = StringListConverter.class)
    private List<String> mediaUrls;

    @Column(length = 500)
    private String tags;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 0:已删除 1:正常 2:违规

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
