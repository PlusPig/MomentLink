package com.keyy.momentlinkback.controller.content;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.request.ContentCreateRequest;
import com.keyy.momentlinkback.dto.request.ContentUpdateRequest;
import com.keyy.momentlinkback.dto.response.ContentDetailResponse;
import com.keyy.momentlinkback.dto.response.ContentResponse;
import com.keyy.momentlinkback.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "内容管理", description = "内容发布、浏览、管理")
@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "发布内容")
    @PostMapping
    public Result<ContentResponse> createContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute ContentCreateRequest request,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return Result.success("发布成功",
                contentService.createContent(userDetails.getUsername(), request, files));
    }

    @Operation(summary = "获取内容列表")
    @GetMapping
    public Result<PageResult<ContentResponse>> getContents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String tags) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return Result.success(contentService.getContents(username, page, size, type, tags));
    }

    @Operation(summary = "获取内容详情")
    @GetMapping("/{id}")
    public Result<ContentDetailResponse> getContentDetail(@PathVariable Long id) {
        return Result.success(contentService.getContentDetail(id));
    }

    @Operation(summary = "更新内容")
    @PutMapping("/{id}")
    public Result<ContentResponse> updateContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ContentUpdateRequest request) {
        return Result.success("更新成功",
                contentService.updateContent(userDetails.getUsername(), id, request));
    }

    @Operation(summary = "删除内容")
    @DeleteMapping("/{id}")
    public Result<String> deleteContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        contentService.deleteContent(userDetails.getUsername(), id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取我的内容")
    @GetMapping("/my")
    public Result<PageResult<ContentResponse>> getMyContents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String tags) {
        return Result.success(
                contentService.getMyContents(userDetails.getUsername(), page, size,
                        startDate, endDate, tags));
    }

    @Operation(summary = "点赞内容")
    @PostMapping("/{id}/like")
    public Result<String> likeContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        contentService.likeContent(userDetails.getUsername(), id);
        return Result.success("点赞成功");
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<String> unlikeContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        contentService.unlikeContent(userDetails.getUsername(), id);
        return Result.success("取消点赞成功");
    }

    @Operation(summary = "评分内容")
    @PostMapping("/{id}/rating")
    public Result<String> rateContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam int score) {
        contentService.rateContent(userDetails.getUsername(), id, score);
        return Result.success("评分成功");
    }
}

//2023152019