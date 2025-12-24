package com.keyy.momentlinkback.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentCreateRequest {
    @NotNull(message = "内容类型不能为空")
    @Min(value = 1, message = "类型值无效")
    @Max(value = 3, message = "类型值无效")
    private Integer type; // 1:图片 2:视频 3:文本

    private String contentText;
    private String tags;
}

//2023152019