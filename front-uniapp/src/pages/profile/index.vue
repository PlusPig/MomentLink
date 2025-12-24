<template>
  <view class="page">
    <!-- 顶部个人信息区域 -->
    <view class="profile-header">
      <view class="profile-content">
        <view class="avatar-wrapper" @click="changeAvatar">
          <image
            class="avatar"
            :src="resolveMediaUrl(userInfo.avatar) || defaultAvatar"
            mode="aspectFill"
          />
          <view class="avatar-edit-hint">
            <iconify-icon icon="mdi:camera" class="camera-icon" />
          </view>
        </view>
        <view class="profile-info">
          <text class="nickname">{{ userInfo.nickname || userInfo.username || '用户' }}</text>
          <text class="username">@{{ userInfo.username || 'username' }}</text>
          <text v-if="userInfo.signature" class="signature">{{ userInfo.signature }}</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="section section-first">
      <view class="cell" @click="goEditProfile">
        <view class="cell-left">
          <view class="cell-icon-wrapper">
            <iconify-icon icon="mdi:account-outline" class="cell-icon" />
          </view>
          <text class="cell-text">编辑资料</text>
        </view>
        <iconify-icon icon="mdi:chevron-right" class="chevron" />
      </view>
      <view class="cell" @click="goChangePassword">
        <view class="cell-left">
          <view class="cell-icon-wrapper">
            <iconify-icon icon="mdi:lock-outline" class="cell-icon" />
          </view>
          <text class="cell-text">修改密码</text>
        </view>
        <iconify-icon icon="mdi:chevron-right" class="chevron" />
      </view>
    </view>

    <view class="section">
      <view class="cell" @click="goMyContents">
        <view class="cell-left">
          <view class="cell-icon-wrapper">
            <iconify-icon icon="mdi:file-document-outline" class="cell-icon" />
          </view>
          <text class="cell-text">我的动态</text>
        </view>
        <view class="cell-right">
          <text class="cell-badge" v-if="contentCount > 0">{{ contentCount }}</text>
          <iconify-icon icon="mdi:chevron-right" class="chevron" />
        </view>
      </view>
    </view>

    <view class="section section-last">
      <view class="cell logout-cell" @click="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getCurrentUser, uploadAvatar } from '@/api/user'
import { fetchMyContents } from '@/api/user'
import { getBaseUrl } from '@/utils/request'

export default {
  data() {
    return {
      userInfo: {},
      contentCount: 0,
      defaultAvatar: '/static/logo.png',
    }
  },
  onShow() {
    const authToken = uni.getStorageSync('authToken')
    if (!authToken) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadUserInfo()
    this.loadContentCount()
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
    async loadUserInfo() {
      try {
        const res = await getCurrentUser()
        const data = res.data || res
        this.userInfo = data.data || data || {}
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    async loadContentCount() {
      try {
        const res = await fetchMyContents({ page: 1, size: 1 })
        const data = res.data || res
        this.contentCount = data.total || data.data?.total || 0
      } catch (e) {
        // 静默失败
      }
    },
    changeAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: async (res) => {
          try {
            await uploadAvatar(res.tempFilePaths[0])
            uni.showToast({ title: '头像上传成功', icon: 'success' })
            this.loadUserInfo()
          } catch (e) {
            uni.showToast({ title: e.message || '上传失败', icon: 'none' })
          }
        },
      })
    },
    goEditProfile() {
      uni.navigateTo({ url: '/pages/profile/edit' })
    },
    goChangePassword() {
      uni.navigateTo({ url: '/pages/profile/password' })
    },
    goMyContents() {
      uni.navigateTo({ url: '/pages/profile/content' })
    },
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('authToken')
            uni.removeStorageSync('user')
            uni.reLaunch({ url: '/pages/login/login' })
          }
        },
      })
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #f5f5f7;
}

/* 顶部个人信息区域 */
.profile-header {
  background-color: #ffffff;
  padding: 80rpx 32rpx 60rpx;
}

.profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 32rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: #f5f5f7;
  border: 3rpx solid #e5e5ea;
}

.avatar-edit-hint {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background-color: #ffffff;
  border: 3rpx solid #e5e5ea;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.camera-icon {
  font-size: 24rpx;
  color: #000000;
}

.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.nickname {
  font-size: 40rpx;
  font-weight: 600;
  color: #000000;
  margin-bottom: 8rpx;
  letter-spacing: -0.5rpx;
}

.username {
  font-size: 26rpx;
  color: #8e8e93;
  margin-bottom: 16rpx;
  font-weight: 400;
}

.signature {
  font-size: 28rpx;
  color: #8e8e93;
  text-align: center;
  line-height: 1.5;
  max-width: 600rpx;
}

/* 功能列表区域 */
.section {
  margin-top: 24rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  margin-left: 20rpx;
  margin-right: 20rpx;
  overflow: hidden;
}

.section-first {
  margin-top: 40rpx;
}

.section-last {
  margin-bottom: 40rpx;
}

.cell {
  padding: 0 24rpx;
  height: 104rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  border-bottom-width: 0.5rpx;
  border-bottom-color: #e5e5ea;
  border-bottom-style: solid;
  transition: background-color 0.2s;
}

.cell:active {
  background-color: #f5f5f7;
}

.cell:last-child {
  border-bottom: none;
}

.cell-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex: 1;
}

.cell-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background-color: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.cell-icon {
  font-size: 32rpx;
  color: #000000;
}

.cell-text {
  font-size: 32rpx;
  color: #000000;
  font-weight: 400;
  letter-spacing: -0.3rpx;
}

.cell-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.cell-badge {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  background-color: #000000;
  color: #ffffff;
  font-weight: 500;
  min-width: 40rpx;
  text-align: center;
}

.chevron {
  font-size: 24rpx;
  color: #c7c7cc;
}

/* 退出登录 */
.logout-cell {
  justify-content: center;
  background-color: #ffffff;
}

.logout-text {
  font-size: 32rpx;
  color: #ff3b30;
  font-weight: 500;
  letter-spacing: -0.3rpx;
}

.logout-cell:active {
  background-color: rgba(255, 59, 48, 0.1);
}
</style>

