<template>
  <view class="page">
    <!-- 顶部操作栏 -->
    <view class="top-actions">
      <view class="top-left">
        <text v-if="unreadCount > 0" class="badge-unread">{{ unreadCount }}</text>
        <text class="page-title">通知</text>
      </view>
      <view class="top-right" @click="markAllRead">
        <iconify-icon icon="mdi:check-all" class="action-icon" />
        <text class="action-text">全部已读</text>
      </view>
    </view>

    <!-- 类型切换标签 -->
    <view class="segmented">
      <view
        v-for="item in typeTabs"
        :key="item.value"
        class="segmented-item"
        :class="{ active: currentType === item.value }"
        @click="switchType(item.value)"
      >
        <iconify-icon :icon="item.icon" class="segmented-icon" />
        <text class="segmented-text">{{ item.label }}</text>
      </view>
    </view>

    <!-- 通知列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="notifications.length === 0 && !loading" class="empty">
        <text class="empty-title">暂无通知</text>
        <text class="empty-subtitle">好友的申请、点赞、评论都会出现在这里</text>
      </view>

      <view
        v-for="item in notifications"
        :key="item.id"
        class="cell"
        :class="{ unread: item.status === 0 }"
        @click="handleClick(item)"
      >
        <view class="cell-left">
          <view class="avatar-wrapper">
            <image
              class="avatar"
              :src="resolveMediaUrl(item.senderAvatar) || defaultAvatar"
              mode="aspectFill"
            />
            <view class="type-icon-wrapper" :class="`type-${item.type}`">
              <iconify-icon :icon="getTypeIcon(item.type)" class="type-icon" />
            </view>
          </view>
          <view class="cell-main">
            <view class="cell-top">
              <text class="sender">{{ item.senderName || '系统' }}</text>
              <text class="time">{{ formatTime(item.createdAt) }}</text>
            </view>
            <view class="cell-message-row">
              <text class="message">{{ item.message }}</text>
              <!-- 好友申请状态显示 -->
              <text v-if="item.type === 1 && item.friendshipStatus !== undefined && item.friendshipStatus !== null" class="friendship-status" :class="getFriendshipStatusClass(item.friendshipStatus)">
                {{ getFriendshipStatusText(item.friendshipStatus) }}
              </text>
            </view>
          </view>
        </view>
        <view class="cell-right">
          <view v-if="item.status === 0" class="dot-unread" />
          <iconify-icon icon="mdi:chevron-right" class="chevron" />
        </view>
      </view>

      <view v-if="loadingMore" class="load-more">
        <text class="load-more-text">加载中...</text>
      </view>
      <view v-else-if="!hasMore && notifications.length > 0" class="load-more">
        <text class="load-more-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import {
  fetchNotifications,
  fetchUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
} from '@/api/notification'
import { getBaseUrl } from '@/utils/request'

export default {
  data() {
    return {
      notifications: [],
      page: 1,
      size: 20,
      hasMore: true,
      loading: false,
      loadingMore: false,
      unreadCount: 0,
      currentType: 0, // 0:全部 1:好友申请 2:点赞 3:评论
      typeTabs: [
        { value: 0, label: '全部', icon: 'mdi:bell-outline' },
        { value: 1, label: '好友申请', icon: 'mdi:account-plus-outline' },
        { value: 2, label: '点赞', icon: 'mdi:heart-outline' },
        { value: 3, label: '评论', icon: 'mdi:comment-outline' },
      ],
      defaultAvatar: '/static/logo.png',
    }
  },
  onShow() {
    const authToken = uni.getStorageSync('authToken')
    if (!authToken) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.refresh()
    this.loadUnreadCount()
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
    async loadUnreadCount() {
      try {
        const res = await fetchUnreadCount()
        const data = res.data || res
        this.unreadCount = data.data || data || 0
      } catch (e) {
        console.error(e)
      }
    },
    async refresh() {
      this.page = 1
      this.hasMore = true
      this.loading = true
      try {
        const list = await this.fetchPage(1)
        this.notifications = list
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      if (!this.hasMore || this.loadingMore || this.loading) return
      this.loadingMore = true
      try {
        const nextPage = this.page + 1
        const list = await this.fetchPage(nextPage)
        if (list.length) {
          this.page = nextPage
          this.notifications = this.notifications.concat(list)
        }
      } finally {
        this.loadingMore = false
      }
    },
    async fetchPage(page) {
      try {
        const params = { page, size: this.size }
        if (this.currentType !== 0) {
          params.type = this.currentType
        }
        const res = await fetchNotifications(params)
        const data = res.data || res
        const pageData = data.data || data
        const list = pageData.list || []
        const total = pageData.total || 0
        this.hasMore = page * this.size < total
        return list
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '通知加载失败', icon: 'none' })
        return []
      }
    },
    switchType(type) {
      if (this.currentType === type) return
      this.currentType = type
      this.refresh()
    },
    formatTime(time) {
      if (!time) return ''
      return String(time).replace('T', ' ').substring(5, 16)
    },
    getTypeIcon(type) {
      const icons = {
        1: 'mdi:account-plus',
        2: 'mdi:heart',
        3: 'mdi:comment',
      }
      return icons[type] || 'mdi:bell'
    },
    getFriendshipStatusText(status) {
      const statusMap = {
        0: '待处理',
        1: '已接受',
        2: '已拒绝',
        3: '已屏蔽',
      }
      return statusMap[status] || ''
    },
    getFriendshipStatusClass(status) {
      const classMap = {
        0: 'status-pending',
        1: 'status-accepted',
        2: 'status-rejected',
        3: 'status-blocked',
      }
      return classMap[status] || ''
    },
    async handleClick(item) {
      try {
        if (item.status === 0) {
          await markNotificationRead(item.id)
          item.status = 1
          if (this.unreadCount > 0) this.unreadCount -= 1
        }
        // 根据类型跳转：好友申请跳详情页，点赞/评论跳内容详情
        if (item.type === 1) {
          // 好友申请跳转到详情页，传递通知相关信息
          const params = new URLSearchParams({
            id: item.id,
            type: item.type,
            senderId: item.senderId || '',
            relatedId: item.relatedId || '',
            senderName: item.senderName || '',
            senderAvatar: item.senderAvatar || '',
            message: item.message || '',
            createdAt: item.createdAt || '',
            status: item.status || 0,
            friendshipStatus: item.friendshipStatus !== undefined && item.friendshipStatus !== null ? item.friendshipStatus : '',
          }).toString()
          uni.navigateTo({
            url: `/pages/notification/detail?${params}`,
          })
        } else if ((item.type === 2 || item.type === 3) && item.contentId) {
          uni.navigateTo({
            url: `/pages/detail/detail?id=${item.contentId}`,
          })
        }
      } catch (e) {
        console.error(e)
      }
    },
    async markAllRead() {
      try {
        await markAllNotificationsRead()
        this.notifications = this.notifications.map((n) => ({
          ...n,
          status: 1,
        }))
        this.unreadCount = 0
        uni.showToast({ title: '已全部标记为已读', icon: 'success' })
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
  },
}
</script>

<style>
.page {
  flex: 1;
  min-height: 100vh;
  background-color: #ffffff;
}

.top-actions {
  padding: 24rpx 32rpx 16rpx;
  background-color: #ffffff;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f3f4f6;
}

.top-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.badge-unread {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  border-radius: 18rpx;
  background-color: #000000;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 600;
  text-align: center;
  line-height: 36rpx;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.top-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.action-icon {
  font-size: 28rpx;
  color: #000000;
}

.action-text {
  font-size: 26rpx;
  color: #000000;
  font-weight: 500;
}

.segmented {
  margin: 16rpx 32rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background-color: #f3f4f6;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 6rpx;
}

.segmented-item {
  flex: 1;
  height: 60rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
}

.segmented-item.active {
  background-color: #000000;
}

.segmented-icon {
  font-size: 24rpx;
  color: #6b7280;
}

.segmented-item.active .segmented-icon {
  color: #ffffff;
}

.segmented-text {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 500;
}

.segmented-item.active .segmented-text {
  color: #ffffff;
  font-weight: 600;
}

.list-scroll {
  padding: 8rpx 0 24rpx;
}

.empty {
  padding: 80rpx 40rpx;
  align-items: center;
  justify-content: center;
  display: flex;
  flex-direction: column;
}

.empty-title {
  font-size: 30rpx;
  color: #111827;
  font-weight: 600;
}

.empty-subtitle {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.cell {
  padding: 24rpx 32rpx;
  background-color: #ffffff;
  display: flex;
  flex-direction: row;
  align-items: center;
  border-bottom-width: 1px;
  border-bottom-color: #f3f4f6;
  border-bottom-style: solid;
}

.cell.unread {
  background-color: #fafafa;
}

.cell-left {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-right: 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background-color: #f3f4f6;
}

.type-icon-wrapper {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #ffffff;
}

.type-icon-wrapper.type-1 {
  background-color: #000000;
}

.type-icon-wrapper.type-2 {
  background-color: #000000;
}

.type-icon-wrapper.type-3 {
  background-color: #000000;
}

.type-icon {
  font-size: 18rpx;
  color: #ffffff;
}

.cell-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cell-top {
  flex-direction: row;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.sender {
  font-size: 30rpx;
  color: #000000;
  font-weight: 600;
}

.time {
  font-size: 22rpx;
  color: #9ca3af;
}

.cell-message-row {
  margin-top: 8rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.message {
  flex: 1;
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.5;
}

.friendship-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 500;
  white-space: nowrap;
}

.friendship-status.status-pending {
  background-color: #fef3c7;
  color: #d97706;
}

.friendship-status.status-accepted {
  background-color: #d1fae5;
  color: #059669;
}

.friendship-status.status-rejected {
  background-color: #fee2e2;
  color: #dc2626;
}

.friendship-status.status-blocked {
  background-color: #f3f4f6;
  color: #6b7280;
}

.cell-right {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.dot-unread {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: #000000;
  margin-right: 12rpx;
}

.chevron {
  font-size: 28rpx;
  color: #d1d5db;
}

.load-more {
  padding: 16rpx 0;
  display: flex;
  justify-content: center;
}

.load-more-text {
  font-size: 24rpx;
  color: #9ca3af;
}
</style>


