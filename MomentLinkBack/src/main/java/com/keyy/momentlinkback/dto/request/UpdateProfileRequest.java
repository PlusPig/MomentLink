package com.keyy.momentlinkback.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "昵称长度不能超过50字")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 11, message = "手机号长度不正确")
    private String phone;

    private Integer gender;

    @Size(max = 200, message = "个性签名长度不能超过200字")
    private String signature;

    @Size(max = 100, message = "地址长度不能超过100字")
    private String location;
}
