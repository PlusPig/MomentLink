package com.keyy.momentlinkback.controller.friend;

import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.request.FriendRequest;
import com.keyy.momentlinkback.dto.response.FriendResponse;
import com.keyy.momentlinkback.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "好友管理", description = "好友关系管理")
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "获取好友列表")
    @GetMapping
    public Result<List<FriendResponse>> getFriendList(
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(
                friendService.getFriendList(userDetails.getUsername()));
    }

    @Operation(summary = "发送好友申请")
    @PostMapping("/request")
    public Result<String> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FriendRequest request) {
        friendService.sendFriendRequest(userDetails.getUsername(), request);
        return Result.success("好友申请已发送");
    }

    @Operation(summary = "接受好友申请")
    @PostMapping("/accept/{friendshipId}")
    public Result<String> acceptFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendshipId) {
        friendService.acceptFriendRequest(userDetails.getUsername(), friendshipId);
        return Result.success("已接受好友申请");
    }

    @Operation(summary = "拒绝好友申请")
    @PostMapping("/reject/{friendshipId}")
    public Result<String> rejectFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendshipId) {
        friendService.rejectFriendRequest(userDetails.getUsername(), friendshipId);
        return Result.success("已拒绝好友申请");
    }

    @Operation(summary = "删除好友")
    @DeleteMapping("/{friendId}")
    public Result<String> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        friendService.deleteFriend(userDetails.getUsername(), friendId);
        return Result.success("已删除好友");
    }

    @Operation(summary = "屏蔽好友")
    @PostMapping("/block/{friendId}")
    public Result<String> blockFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        friendService.blockFriend(userDetails.getUsername(), friendId);
        return Result.success("已屏蔽该好友");
    }

    @Operation(summary = "修改好友备注")
    @PutMapping("/{friendId}/remark")
    public Result<String> updateRemark(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId,
            @RequestParam String remark) {
        friendService.updateRemark(userDetails.getUsername(), friendId, remark);
        return Result.success("备注修改成功");
    }
}
