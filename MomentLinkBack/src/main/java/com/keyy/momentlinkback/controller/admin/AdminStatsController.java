package com.keyy.momentlinkback.controller.admin;

import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.response.StatsResponse;
import com.keyy.momentlinkback.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-数据统计", description = "数据大屏统计接口")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final StatsService statsService;

    @Operation(summary = "获取总体统计数据")
    @GetMapping("/overview")
    public Result<StatsResponse> getOverviewStats() {
        return Result.success(statsService.getOverviewStats());
    }

    @Operation(summary = "获取用户增长趋势")
    @GetMapping("/user-growth")
    public Result<?> getUserGrowthStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getUserGrowthStats(startDate, endDate));
    }

    @Operation(summary = "获取内容发布趋势")
    @GetMapping("/content-trend")
    public Result<?> getContentTrendStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getContentTrendStats(startDate, endDate));
    }

    @Operation(summary = "获取活跃用户统计")
    @GetMapping("/active-users")
    public Result<?> getActiveUsersStats(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.getActiveUsersStats(days));
    }

    @Operation(summary = "获取热门内容")
    @GetMapping("/hot-contents")
    public Result<?> getHotContents(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.getHotContents(limit));
    }
}
