package com.keyy.momentlinkback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FriendRequest {

    @NotBlank(message = "用户帐号不能为空")
    private String username;

    @Size(max = 200, message = "验证消息长度不能超过200字")
    private String message; // 可以为空或null
}
