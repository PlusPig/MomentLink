package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.dto.request.FriendRequest;
import com.keyy.momentlinkback.dto.response.FriendResponse;
import com.keyy.momentlinkback.entity.Friendship;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.exception.BadRequestException;
import com.keyy.momentlinkback.exception.ResourceNotFoundException;
import com.keyy.momentlinkback.exception.UnauthorizedException;
import com.keyy.momentlinkback.repository.FriendshipRepository;
import com.keyy.momentlinkback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<FriendResponse> getFriendList(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 调试：查询所有状态的好友关系
        List<Friendship> allFriendships = new ArrayList<>();
        for (Friendship f : friendshipRepository.findAll()) {
            if (f.getUserId().equals(user.getId()) || f.getFriendId().equals(user.getId())) {
                allFriendships.add(f);
            }
        }
        System.out.println("用户 " + username + " (ID: " + user.getId() + ") 的所有好友关系:");
        for (Friendship f : allFriendships) {
            System.out.println("  Friendship ID: " + f.getId() + 
                    ", userId: " + f.getUserId() + 
                    ", friendId: " + f.getFriendId() + 
                    ", status: " + f.getStatus());
        }

        List<Friendship> friendships = friendshipRepository
                .findAllFriendshipsByStatuses(user.getId(), List.of(1, 3));
        
        System.out.println("查询 status=1 的好友关系数量: " + friendships.size());

        List<FriendResponse> responses = new ArrayList<>();
        for (Friendship friendship : friendships) {
            Long friendId = friendship.getUserId().equals(user.getId())
                    ? friendship.getFriendId() : friendship.getUserId();

            User friend = userRepository.findById(friendId).orElse(null);
            if (friend != null) {
                FriendResponse response = convertToResponse(friendship, friend);
                responses.add(response);
            }
        }

        return responses;
    }

    @Transactional
    public void sendFriendRequest(String username, FriendRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        User friend = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("目标用户不存在"));

        if (user.getId().equals(friend.getId())) {
            throw new BadRequestException("不能添加自己为好友");
        }

        // 检查是否已经存在好友关系（只检查待接受或已接受的状态）
        Optional<Friendship> existing1 = friendshipRepository.findByUserIdAndFriendId(user.getId(), friend.getId());
        Optional<Friendship> existing2 = friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId());
        
        if (existing1.isPresent()) {
            Friendship fs = existing1.get();
            if (fs.getStatus() == 0 || fs.getStatus() == 1) {
                throw new BadRequestException("已经是好友或申请已发送");
            }
        }
        if (existing2.isPresent()) {
            Friendship fs = existing2.get();
            if (fs.getStatus() == 0 || fs.getStatus() == 1) {
                throw new BadRequestException("已经是好友或申请已发送");
            }
        }

        Friendship friendship = new Friendship();
        friendship.setUserId(user.getId());
        friendship.setFriendId(friend.getId());
        friendship.setStatus(0); // 待接受
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // 发送通知，传递 friendshipId
        String message = user.getNickname() + " 请求添加你为好友";
        if (request.getMessage() != null) {
            message += "：" + request.getMessage();
        }
        notificationService.createNotification(
                friend.getId(), 1, user.getId(), null, savedFriendship.getId(), message);
    }

    @Transactional
    public void acceptFriendRequest(String username, Long friendshipId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("好友申请不存在"));

        if (!friendship.getFriendId().equals(user.getId())) {
            throw new UnauthorizedException("无权限操作此申请");
        }

        if (friendship.getStatus() != 0) {
            throw new BadRequestException("该申请已处理");
        }

        friendship.setStatus(1); // 已接受
        Friendship saved = friendshipRepository.save(friendship);
        System.out.println("接受好友申请后保存的记录: ID=" + saved.getId() + 
                ", userId=" + saved.getUserId() + 
                ", friendId=" + saved.getFriendId() + 
                ", status=" + saved.getStatus());

        // 发送通知
        notificationService.createNotification(
                friendship.getUserId(), 1, user.getId(), null,
                user.getNickname() + " 接受了你的好友申请");
    }

    @Transactional
    public void rejectFriendRequest(String username, Long friendshipId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("好友申请不存在"));

        if (!friendship.getFriendId().equals(user.getId())) {
            throw new UnauthorizedException("无权限操作此申请");
        }

        friendship.setStatus(2); // 已拒绝
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void deleteFriend(String username, Long friendId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Friendship friendship = friendshipRepository
                .findByUserIdAndFriendId(user.getId(), friendId)
                .or(() -> friendshipRepository.findByUserIdAndFriendId(friendId, user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("好友关系不存在"));

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void blockFriend(String username, Long friendId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Friendship friendship = friendshipRepository
                .findByUserIdAndFriendId(user.getId(), friendId)
                .or(() -> friendshipRepository.findByUserIdAndFriendId(friendId, user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("好友关系不存在"));

        friendship.setStatus(3); // 已屏蔽
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void updateRemark(String username, Long friendId, String remark) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Friendship friendship = friendshipRepository
                .findByUserIdAndFriendId(user.getId(), friendId)
                .orElseThrow(() -> new ResourceNotFoundException("好友关系不存在"));

        friendship.setRemark(remark);
        friendshipRepository.save(friendship);
    }

    private FriendResponse convertToResponse(Friendship friendship, User friend) {
        FriendResponse response = new FriendResponse();
        response.setId(friendship.getId());
        response.setUserId(friendship.getUserId());
        response.setFriendId(friend.getId());
        response.setUsername(friend.getUsername());
        response.setNickname(friend.getNickname());
        response.setAvatar(friend.getAvatar());
        response.setSignature(friend.getSignature());
        response.setRemark(friendship.getRemark());
        response.setStatus(friendship.getStatus());
        response.setCreatedAt(friendship.getCreatedAt());
        return response;
    }
}
