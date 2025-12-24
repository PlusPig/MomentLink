package com.keyy.momentlinkback.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentDetailResponse extends ContentResponse {
    private List<CommentResponse> comments;
}
