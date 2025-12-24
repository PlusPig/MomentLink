<template>
  <view class="page">
    <!-- 顶部栏：标题 + 添加好友 -->
    <view class="top-bar">
      <view class="top-left">
        <text class="top-title">好友</text>
        <text v-if="friends.length > 0" class="top-count">{{ friends.length }}</text>
      </view>
      <view class="top-add" @click="openAddFriend">
        <iconify-icon icon="mdi:account-plus-outline" class="top-add-icon" />
      </view>
    </view>

    <!-- 顶部搜索栏（Apple 风格） -->
    <view class="search-bar">
      <view class="search-inner">
        <iconify-icon icon="mdi:magnify" class="search-icon" />
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索好友或备注"
          placeholder-style="color:#9ca3af"
        />
        <view v-if="keyword" class="search-clear" @click="keyword = ''">
          <iconify-icon icon="mdi:close-circle" class="clear-icon" />
        </view>
      </view>
    </view>

    <!-- 好友列表 -->
    <scroll-view scroll-y class="list-scroll">
      <view v-if="filteredFriends.length === 0 && !loading" class="empty">
        <text class="empty-title">还没有好友</text>
        <text class="empty-subtitle">添加一些好友，一起分享生活</text>
      </view>

      <view
        v-for="item in filteredFriends"
        :key="item.id"
        class="cell"
        @click="goDetail(item)"
      >
        <image
          class="avatar"
          :src="resolveMediaUrl(item.avatar) || defaultAvatar"
          mode="aspectFill"
        />
        <view class="cell-main">
          <view class="cell-top">
            <text class="name">
              {{ item.remark || item.nickname || item.username || '好友' }}
            </text>
            <text
              v-if="item.remark && (item.nickname || item.username)"
              class="sub-name"
            >
              {{ item.nickname || item.username }}
            </text>
          </view>
          <text v-if="item.signature" class="signature" number-of-lines="1">
            {{ item.signature }}
          </text>
        </view>
        <view class="cell-right">
          <text v-if="item.status === 3" class="badge badge-blocked">已屏蔽</text>
          <iconify-icon icon="mdi:chevron-right" class="chevron" />
        </view>
      </view>
    </scroll-view>

    <!-- 添加好友弹窗 -->
    <BottomModal :show="showAddFriendModal" title="添加好友" @close="closeAddFriendModal">
      <view class="add-friend-form">
        <input
          class="friend-input"
          v-model="friendIdInput"
          placeholder="输入对方用户帐号"
          placeholder-style="color:#9ca3af"
        />
        <input
          class="friend-input"
          v-model="friendMessageInput"
          placeholder="备注说明（可选）"
          placeholder-style="color:#9ca3af"
        />
        <button class="add-friend-btn" @click="submitAddFriend">发送申请</button>
      </view>
    </BottomModal>
  </view>
</template>

<script>
import { fetchFriends, sendFriendRequest } from '@/api/friend'
import BottomModal from '@/components/BottomModal.vue'
import { getBaseUrl } from '@/utils/request'

export default {
  components: {
    BottomModal,
  },
  data() {
    return {
      friends: [],
      loading: false,
      keyword: '',
      defaultAvatar: '/static/logo.png',
      showAddFriendModal: false,
      friendIdInput: '',
      friendMessageInput: '',
    }
  },
  onShow() {
    const authToken = uni.getStorageSync('authToken')
    if (!authToken) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 每次显示页面时刷新好友列表
    this.loadFriends()
  },
  onLoad() {
    const authToken = uni.getStorageSync('authToken')
    if (!authToken) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadFriends()
  },
  computed: {
    filteredFriends() {
      if (!this.keyword) return this.friends
      const k = this.keyword.toLowerCase()
      return this.friends.filter((f) => {
        const remark = (f.remark || '').toLowerCase()
        const nickname = (f.nickname || '').toLowerCase()
        const username = (f.username || '').toLowerCase()
        return remark.includes(k) || nickname.includes(k) || username.includes(k)
      })
    },
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
    async loadFriends() {
      this.loading = true
      try {
        const res = await fetchFriends()
        console.log('好友列表API响应:', res)
        const data = res.data || res
        const friendList = data.data || data || []
        console.log('解析后的好友列表:', friendList)
        this.friends = friendList
        console.log('设置的好友列表:', this.friends)
      } catch (e) {
        console.error('加载好友列表失败:', e)
        uni.showToast({ title: '好友加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    openAddFriend() {
      this.friendIdInput = ''
      this.friendMessageInput = ''
      this.showAddFriendModal = true
    },
    closeAddFriendModal() {
      this.showAddFriendModal = false
      this.friendIdInput = ''
      this.friendMessageInput = ''
    },
    async submitAddFriend() {
      const username = this.friendIdInput.trim()
      if (!username) {
        uni.showToast({ title: '请输入对方用户帐号', icon: 'none' })
        return
      }
      try {
        const payload = {
          username,
          message: this.friendMessageInput.trim() || '',
        }
        console.log('发送好友申请，payload:', payload)
        await sendFriendRequest(payload)
        uni.showToast({ title: '好友申请已发送', icon: 'success' })
        this.closeAddFriendModal()
      } catch (e) {
        console.error('发送好友申请失败:', e)
        console.error('错误详情:', e.message)
        const errorMsg = e.message || '发送失败'
        // 如果是"已经是好友或申请已发送"，提示更友好的信息
        if (errorMsg.includes('已经是好友') || errorMsg.includes('申请已发送')) {
          uni.showToast({ title: '该用户已经是您的好友或申请已发送', icon: 'none', duration: 2000 })
        } else {
          uni.showToast({ title: errorMsg, icon: 'none', duration: 3000 })
        }
      }
    },
    goDetail(item) {
      // 确保 friendId 存在，如果不存在则使用 id
      const friendId = item.friendId || item.id
      if (!friendId) {
        uni.showToast({ title: '好友信息错误', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/pages/friend/detail?id=${friendId}&remark=${encodeURIComponent(
          item.remark || ''
        )}`,
      })
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

.top-bar {
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
  align-items: baseline;
  gap: 12rpx;
}

.top-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.top-count {
  font-size: 24rpx;
  color: #9ca3af;
  font-weight: 500;
}

.top-add {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: #000000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.top-add-icon {
  font-size: 32rpx;
  color: #ffffff;
}

.search-bar {
  padding: 16rpx 32rpx;
  background-color: #ffffff;
}

.search-inner {
  height: 72rpx;
  border-radius: 36rpx;
  background-color: #f3f4f6;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 0 20rpx;
}

.search-icon {
  font-size: 32rpx;
  color: #9ca3af;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #000000;
}

.search-clear {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8rpx;
}

.clear-icon {
  font-size: 32rpx;
  color: #9ca3af;
}

.list-scroll {
  padding: 8rpx 0 24rpx;
}

.empty {
  padding: 120rpx 40rpx;
  align-items: center;
  justify-content: center;
  display: flex;
  flex-direction: column;
}

.empty-title {
  font-size: 32rpx;
  color: #000000;
  font-weight: 600;
}

.empty-subtitle {
  margin-top: 16rpx;
  font-size: 26rpx;
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

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;
  margin-right: 20rpx;
  background-color: #f3f4f6;
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
}

.name {
  font-size: 32rpx;
  color: #000000;
  font-weight: 600;
}

.sub-name {
  margin-left: 10rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.signature {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.4;
}

.cell-right {
  flex-direction: row;
  display: flex;
  align-items: center;
}

.badge {
  font-size: 22rpx;
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.badge-blocked {
  background-color: #f3f4f6;
  color: #6b7280;
}

.chevron {
  font-size: 28rpx;
  color: #d1d5db;
  margin-left: 12rpx;
}

.add-friend-form {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.friend-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background-color: #f3f4f6;
  font-size: 30rpx;
  color: #111827;
  box-sizing: border-box;
}

.add-friend-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  background-color: #000000;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  margin-top: 8rpx;
}
</style>



