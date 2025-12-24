// ContentResponse.java
package com.keyy.momentlinkback.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer type;
    private String contentText;
    private List<String> mediaUrls;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Double avgRating;
    private Integer ratingCount;
    private Boolean isLiked;
    private Integer myRating;
    private LocalDateTime createdAt;
}