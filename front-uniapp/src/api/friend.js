import { request } from '@/utils/request'

// 获取好友列表
export const fetchFriends = () => request.get('/friends')

// 删除好友
export const deleteFriend = (friendId) =>
  request({
    url: `/friends/${friendId}`,
    method: 'DELETE',
  })

// 屏蔽好友
export const blockFriend = (friendId) =>
  request.post(`/friends/block/${friendId}`)

// 修改备注（后端用 @RequestParam remark）
export const updateRemark = (friendId, remark) =>
  request({
    url: `/friends/${friendId}/remark?remark=${encodeURIComponent(remark)}`,
    method: 'PUT',
  })

// 发送好友请求
export const sendFriendRequest = (payload) =>
  request.post('/friends/request', payload)

// 接受好友申请
export const acceptFriendRequest = (friendshipId) =>
  request.post(`/friends/accept/${friendshipId}`)

// 拒绝好友申请
export const rejectFriendRequest = (friendshipId) =>
  request.post(`/friends/reject/${friendshipId}`)


