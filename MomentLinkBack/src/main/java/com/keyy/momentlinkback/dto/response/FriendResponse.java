package com.keyy.momentlinkback.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendResponse {
    private Long id;
    private Long userId;
    private Long friendId;
    private String username;
    private String nickname;
    private String avatar;
    private String signature;
    private String remark;
    private Integer status; // 0:待接受 1:已接受 2:已拒绝 3:已屏蔽
    private LocalDateTime createdAt;
}
