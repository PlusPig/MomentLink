package com.keyy.momentlinkback.dto.request;

import lombok.Data;

@Data
public class RatingRequest {
    private Long contentId;
    private Integer score;
}