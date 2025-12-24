package com.keyy.momentlinkback.controller.content;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.request.CommentRequest;
import com.keyy.momentlinkback.dto.response.CommentResponse;
import com.keyy.momentlinkback.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论管理", description = "内容评论相关接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentResponse> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CommentRequest request) {
        return Result.success("评论成功",
                commentService.createComment(userDetails.getUsername(), request));
    }

    @Operation(summary = "获取内容的评论列表")
    @GetMapping("/content/{contentId}")
    public Result<PageResult<CommentResponse>> getCommentsByContent(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(
                commentService.getCommentsByContent(contentId, page, size));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<String> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        commentService.deleteComment(userDetails.getUsername(), id);
        return Result.success("删除成功");
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/{id}/like")
    public Result<String> likeComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        commentService.likeComment(userDetails.getUsername(), id);
        return Result.success("点赞成功");
    }
}
