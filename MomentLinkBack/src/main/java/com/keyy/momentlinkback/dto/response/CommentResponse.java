// CommentResponse.java
package com.keyy.momentlinkback.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private Long contentId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String userAvatar; // 添加字段
    private Long parentId;
    private Long replyToUserId;
    private String replyToUsername;
    private String commentText;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;

    // 添加setter方法
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}