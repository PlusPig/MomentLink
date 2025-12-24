// StatsResponse.java
package com.keyy.momentlinkback.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalContents;
    private Long todayContents;
    private Long totalComments;
    private Long totalLikes;
    private Double avgRating;
}
