<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <!-- 头部卡片：申请人信息 -->
      <view class="card-header">
        <image class="avatar" :src="resolveMediaUrl(senderInfo.avatar) || defaultAvatar" mode="aspectFill" />
        <view class="header-text">
          <text class="name">{{ senderInfo.nickname || senderInfo.username || '用户' }}</text>
          <text v-if="senderInfo.username && senderInfo.nickname" class="sub-name">
            {{ senderInfo.username }}
          </text>
          <text v-if="senderInfo.signature" class="signature">
            {{ senderInfo.signature }}
          </text>
        </view>
      </view>

      <!-- 申请信息 -->
      <view class="card">
        <view class="info-row">
          <view class="info-label">
            <iconify-icon icon="mdi:account-plus" class="label-icon" />
            <text class="label-text">申请时间</text>
          </view>
          <text class="info-value">{{ formatTime(notification.createdAt) }}</text>
        </view>
        <view class="info-row">
          <view class="info-label">
            <iconify-icon icon="mdi:message-text" class="label-icon" />
            <text class="label-text">申请消息</text>
          </view>
          <text class="info-value">{{ notification.message || '无' }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view v-if="!notification.friendshipStatus || notification.friendshipStatus === 0" class="card card-actions">
        <button
          class="btn btn-accept"
          :disabled="processing"
          @click="handleAccept"
        >
          <iconify-icon icon="mdi:check" class="btn-icon" />
          <text class="btn-text">接受申请</text>
        </button>
        <button
          class="btn btn-reject"
          :disabled="processing"
          @click="handleReject"
        >
          <iconify-icon icon="mdi:close" class="btn-icon" />
          <text class="btn-text">拒绝申请</text>
        </button>
      </view>

      <!-- 状态提示 -->
      <view v-if="notification.friendshipStatus !== null && notification.friendshipStatus !== undefined && notification.friendshipStatus !== 0" class="card card-status">
        <view class="status-info">
          <iconify-icon
            :icon="notification.friendshipStatus === 1 ? 'mdi:check-circle' : notification.friendshipStatus === 2 ? 'mdi:close-circle' : 'mdi:block'"
            class="status-icon"
            :class="{
              'status-accepted': notification.friendshipStatus === 1,
              'status-rejected': notification.friendshipStatus === 2,
              'status-blocked': notification.friendshipStatus === 3,
            }"
          />
          <text class="status-text">
            {{ notification.friendshipStatus === 1 ? '已接受' : notification.friendshipStatus === 2 ? '已拒绝' : notification.friendshipStatus === 3 ? '已屏蔽' : '未知状态' }}
          </text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { acceptFriendRequest, rejectFriendRequest } from '@/api/friend'
import { markNotificationRead } from '@/api/notification'
import { getUserById, getCurrentUser } from '@/api/user'
import { request, getBaseUrl } from '@/utils/request'

export default {
  data() {
    return {
      notification: {},
      senderInfo: {},
      friendshipId: null,
      processing: false,
      defaultAvatar: '/static/logo.png',
    }
  },
  async onLoad(options) {
    const { id, type, senderId, friendshipId, relatedId, senderName, senderAvatar, message, createdAt, status, friendshipStatus } = options
    if (type === '1' && senderId) {
      // 使用传递过来的通知信息
      const friendshipStatusNum = friendshipStatus !== undefined && friendshipStatus !== null && friendshipStatus !== '' ? Number(friendshipStatus) : null
      this.notification = {
        id,
        type,
        senderId,
        senderName,
        senderAvatar,
        message,
        createdAt,
        status: status ? Number(status) : 0,
        friendshipStatus: friendshipStatusNum,
      }
      // 优先使用 relatedId（从后端返回的 friendshipId），其次使用 friendshipId（从URL参数传递的）
      this.friendshipId = relatedId || friendshipId || null
      if (!this.friendshipId) {
        await this.findFriendshipId(senderId)
      }
      this.loadSenderInfo(senderId)
    } else {
      uni.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  methods: {
    resolveMediaUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url

      const baseUrl = getBaseUrl()

      if (url.startsWith('/api/')) {
        if (baseUrl === '/api') return url
        const hostRoot = baseUrl.endsWith('/api') ? baseUrl.slice(0, -4) : baseUrl
        return `${hostRoot}${url}`
      }

      if (url.startsWith('/uploads/')) {
        return `${baseUrl}${url}`
      }

      return `${baseUrl}${url.startsWith('/') ? url : '/' + url}`
    },
    async findFriendshipId(senderId) {
      try {
        // 获取当前用户信息
        const currentUserRes = await getCurrentUser()
        const currentUser = currentUserRes.data || currentUserRes
        const currentUserId = currentUser.data?.id || currentUser.id

        if (!currentUserId || !senderId) {
          console.error('缺少必要参数')
          return
        }

        // 尝试通过好友列表查找待接受的好友申请
        // 注意：好友申请中，senderId 是申请人，currentUserId 是被申请人
        // 所以 friendship 的 userId = senderId, friendId = currentUserId, status = 0
        // 由于后端没有直接返回 friendshipId 的接口，这里先尝试查找
        // 如果找不到，会在用户点击接受/拒绝时提示错误
        // 实际项目中，后端应该在通知响应中包含 friendshipId
        console.log('需要 friendshipId，但后端未提供，将在操作时处理')
      } catch (e) {
        console.error('查找 friendshipId 失败:', e)
      }
    },
    async loadSenderInfo(senderId) {
      if (!senderId) {
        // 如果没有senderId，使用通知中的信息
        this.senderInfo = {
          nickname: this.notification.senderName,
          username: '',
          avatar: this.notification.senderAvatar,
          signature: '',
        }
        return
      }
      try {
        const res = await getUserById(senderId)
        const data = res.data || res
        this.senderInfo = data.data || data || {
          nickname: this.notification.senderName,
          username: '',
          avatar: this.notification.senderAvatar,
          signature: '',
        }
      } catch (e) {
        console.error(e)
        // 失败时使用通知中的信息
        this.senderInfo = {
          nickname: this.notification.senderName,
          username: '',
          avatar: this.notification.senderAvatar,
          signature: '',
        }
      }
    },
    async handleAccept() {
      if (this.processing) return
      if (!this.friendshipId) {
        uni.showToast({ title: '无法找到好友申请记录', icon: 'none' })
        return
      }
      this.processing = true
      try {
        await acceptFriendRequest(this.friendshipId)
        await markNotificationRead(this.notification.id)
        this.notification.status = 1
        this.notification.friendshipStatus = 1 // 更新好友申请状态为已接受
        uni.showToast({ title: '已接受好友申请', icon: 'success' })
        // 通知好友列表页面刷新
        setTimeout(() => {
          const pages = getCurrentPages()
          const friendListPage = pages.find((page) => page.route === 'pages/friend/list')
          if (friendListPage && friendListPage.$vm) {
            friendListPage.$vm.loadFriends()
          }
          uni.navigateBack()
        }, 1500)
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '操作失败', icon: 'none' })
      } finally {
        this.processing = false
      }
    },
    async handleReject() {
      if (this.processing) return
      if (!this.friendshipId) {
        uni.showToast({ title: '无法找到好友申请记录', icon: 'none' })
        return
      }
      uni.showModal({
        title: '确认拒绝',
        content: '确定要拒绝这个好友申请吗？',
        confirmText: '拒绝',
        confirmColor: '#000000',
        success: async (res) => {
          if (res.confirm) {
            this.processing = true
            try {
              await rejectFriendRequest(this.friendshipId)
              await markNotificationRead(this.notification.id)
              this.notification.status = 1 // 通知标记为已读
              this.notification.friendshipStatus = 2 // 更新好友申请状态为已拒绝
              uni.showToast({ title: '已拒绝好友申请', icon: 'success' })
              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            } catch (e) {
              console.error(e)
              uni.showToast({ title: e.message || '操作失败', icon: 'none' })
            } finally {
              this.processing = false
            }
          }
        },
      })
    },
    formatTime(time) {
      if (!time) return ''
      return String(time).replace('T', ' ').substring(0, 16)
    },
  },
}
</script>

<style>
.page {
  flex: 1;
  min-height: 100vh;
  background-color: #f3f4f6;
}

.scroll {
  padding: 16rpx 0 24rpx;
}

.card-header {
  margin: 0 32rpx 16rpx;
  padding: 48rpx 32rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 30rpx;
  margin-right: 24rpx;
  background-color: #f3f4f6;
}

.header-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 36rpx;
  font-weight: 700;
  color: #000000;
  margin-bottom: 8rpx;
}

.sub-name {
  font-size: 26rpx;
  color: #9ca3af;
  margin-bottom: 12rpx;
}

.signature {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.5;
}

.card {
  margin: 0 32rpx 16rpx;
  padding: 32rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
}

.info-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.label-icon {
  font-size: 28rpx;
  color: #000000;
}

.label-text {
  font-size: 28rpx;
  color: #000000;
  font-weight: 500;
}

.info-value {
  font-size: 28rpx;
  color: #6b7280;
  text-align: right;
  flex: 1;
  margin-left: 24rpx;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 32rpx;
}

.btn {
  width: 100%;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
}

.btn:disabled {
  opacity: 0.5;
}

.btn-accept {
  background-color: #000000;
  color: #ffffff;
}

.btn-reject {
  background-color: #f3f4f6;
  color: #000000;
}

.btn-icon {
  font-size: 32rpx;
}

.btn-text {
  font-size: 32rpx;
  font-weight: 600;
}

.card-status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.status-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.status-icon {
  font-size: 40rpx;
}

.status-icon.status-accepted {
  color: #000000;
}

.status-icon.status-rejected {
  color: #9ca3af;
}

.status-text {
  font-size: 28rpx;
  color: #6b7280;
  font-weight: 500;
}
</style>

