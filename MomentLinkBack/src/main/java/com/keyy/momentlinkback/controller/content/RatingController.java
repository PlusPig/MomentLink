package com.keyy.momentlinkback.controller.content;

import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.request.RatingRequest;
import com.keyy.momentlinkback.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "内容评分", description = "内容评分相关接口")
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "为内容评分")
    @PostMapping
    public Result<String> rateContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RatingRequest request) {
        ratingService.rateContent(userDetails.getUsername(),
                request.getContentId(), request.getScore());
        return Result.success("评分成功");
    }

    @Operation(summary = "获取我的评分")
    @GetMapping("/my")
    public Result<Object> getMyRatings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Object ratings = ratingService.getMyRatings(userDetails.getUsername(), page, size);
        return Result.success(ratings);
    }

    @Operation(summary = "获取内容的评分详情")
    @GetMapping("/content/{contentId}")
    public Result<Object> getContentRatings(@PathVariable Long contentId) {
        Object ratingDetail = ratingService.getContentRatings(contentId);
        return Result.success(ratingDetail);
    }

    @Operation(summary = "删除评分")
    @DeleteMapping("/{contentId}")
    public Result<String> deleteRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long contentId) {
        ratingService.deleteRating(userDetails.getUsername(), contentId);
        return Result.success("删除成功");
    }
}
