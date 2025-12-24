package com.keyy.momentlinkback.controller.notification;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.response.NotificationResponse;
import com.keyy.momentlinkback.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理", description = "系统通知管理")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<PageResult<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        return Result.success(
                notificationService.getNotifications(
                        userDetails.getUsername(), page, size, type, status));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(
                notificationService.getUnreadCount(userDetails.getUsername()));
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(userDetails.getUsername(), id);
        return Result.success("已标记为已读");
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read/all")
    public Result<String> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return Result.success("已全部标记为已读");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.deleteNotification(userDetails.getUsername(), id);
        return Result.success("删除成功");
    }
}
