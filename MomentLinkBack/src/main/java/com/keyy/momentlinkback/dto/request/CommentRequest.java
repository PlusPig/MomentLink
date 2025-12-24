package com.keyy.momentlinkback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    private Long parentId = 0L;
    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    private String commentText;
}
