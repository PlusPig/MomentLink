package com.keyy.momentlinkback.controller.admin;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.response.ContentResponse;
import com.keyy.momentlinkback.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-内容管理", description = "管理员内容管理接口")
@RestController
@RequestMapping("/admin/contents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentController {

    private final ContentService contentService;

    @Operation(summary = "查询所有内容")
    @GetMapping
    public Result<PageResult<ContentResponse>> getAllContents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        return Result.success(
                contentService.getAllContents(page, size, userId, type, status));
    }

    @Operation(summary = "删除违规内容")
    @DeleteMapping("/{id}")
    public Result<String> deleteContent(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        contentService.deleteContentByAdmin(id, reason);
        return Result.success("内容已删除,已通知用户");
    }

    @Operation(summary = "标记内容为违规")
    @PutMapping("/{id}/violation")
    public Result<String> markAsViolation(
            @PathVariable Long id,
            @RequestParam String reason) {
        contentService.markAsViolation(id, reason);
        return Result.success("已标记为违规");
    }
}
