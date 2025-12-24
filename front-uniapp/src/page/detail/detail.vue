<template>
  <view class="detail-page">
    <scroll-view scroll-y class="detail-scroll">
      <!-- 内容卡片 -->
      <view v-if="content" class="card">
        <view class="card-header">
          <image
            class="avatar"
            :src="resolveMediaUrl(content.avatar) || defaultAvatar"
            mode="aspectFill"
          />
          <view class="header-info">
            <text class="nickname">{{ content.nickname || content.username || '用户' }}</text>
            <text class="time">{{ formatTime(content.createdAt) }}</text>
          </view>
        </view>

        <view v-if="content.contentText" class="card-content-text">
          <text>{{ content.contentText }}</text>
        </view>

        <view v-if="content.mediaUrls && content.mediaUrls.length" class="card-media">
          <block v-if="content.type === 1">
            <view class="image-grid">
              <view
                v-for="(url, idx) in content.mediaUrls"
                :key="idx"
                class="image-item"
                @click.stop="previewImage(content.mediaUrls, idx)"
              >
                <image class="image-inner" :src="resolveMediaUrl(url)" mode="aspectFill" />
              </view>
            </view>
          </block>
          <block v-else-if="content.type === 2">
            <video
              class="video-player"
              :src="resolveMediaUrl(content.mediaUrls[0])"
              controls
            />
          </block>
        </view>

        <view v-if="content.tags && content.tags.length" class="tags">
          <text v-for="(tag, idx) in content.tags" :key="idx" class="tag-chip">
            #{{ tag }}
          </text>
        </view>

        <!-- 数据 & 操作 -->
        <view class="meta-bar">
          <view class="meta-left">
            <view class="meta-chip">
              <iconify-icon icon="mdi:eye-outline" class="meta-icon" />
              <text class="meta-item">{{ content.viewCount || 0 }}</text>
            </view>
            <text class="meta-dot">·</text>
            <view class="meta-chip">
              <iconify-icon icon="mdi:heart-outline" class="meta-icon" />
              <text class="meta-item">{{ content.likeCount || 0 }}</text>
            </view>
            <text class="meta-dot">·</text>
            <view class="meta-chip">
              <iconify-icon icon="mdi:star" class="meta-icon meta-icon-star" />
              <text class="meta-item">
                {{ (content.avgRating || 0).toFixed(1) }} · {{ content.ratingCount || 0 }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 评论区域 -->
      <view class="comment-section">
        <view class="comment-header">
          <text class="comment-title">评论</text>
          <text class="comment-count">{{ totalComments }} 条</text>
        </view>

        <view v-if="comments.length === 0 && !loadingComments" class="comment-empty">
          <text class="comment-empty-text">还没有评论，抢先说点什么吧～</text>
        </view>

        <view
          v-for="item in comments"
          :key="item.id"
          class="comment-item"
        >
          <image
            class="comment-avatar"
            :src="resolveMediaUrl(item.avatar || item.userAvatar) || defaultAvatar"
            mode="aspectFill"
          />
          <view class="comment-body">
            <view class="comment-top-row">
              <text class="comment-nickname">
                {{ item.nickname || item.username || '用户' }}
              </text>
              <text class="comment-time">{{ formatTime(item.createdAt) }}</text>
            </view>
            <text class="comment-text">{{ item.commentText }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入栏 - 固定在底部 -->
    <view class="comment-input-bar">
      <view class="input-wrapper">
        <input
          class="comment-input"
          v-model="commentText"
          placeholder="写评论..."
          placeholder-style="color:#8e8e93"
          confirm-type="send"
          @confirm="sendComment"
        />
        <button 
          class="comment-send" 
          :disabled="!commentText.trim() || sendingComment"
          :loading="sendingComment" 
          @click="sendComment"
        >
          发送
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { fetchContentDetail } from '@/api/content'
import { fetchCommentsByContent, createComment } from '@/api/comment'
import { getBaseUrl } from '@/utils/request'

export default {
  data() {
    return {
      id: null,
      content: null,
      comments: [],
      totalComments: 0,
      loadingContent: false,
      loadingComments: false,
      sendingComment: false,
      commentText: '',
      defaultAvatar: '/static/logo.png',
    }
  },
  onLoad(options) {
    this.id = Number(options.id)
    if (!this.id) {
      uni.showToast({ title: '内容不存在', icon: 'none' })
      setTimeout(() => uni.navigateBack({ delta: 1 }), 800)
      return
    }
    this.loadContent()
    this.loadComments()
  },
  methods: {
    async loadContent() {
      this.loadingContent = true
      try {
        const res = await fetchContentDetail(this.id)
        const data = res.data || res
        const detail = data.data || data
        if (detail && typeof detail.tags === 'string') {
          detail.tags = detail.tags
            .split(',')
            .map((t) => t.trim())
            .filter(Boolean)
        }
        this.content = detail
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loadingContent = false
      }
    },
    resolveMediaUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url

      const baseUrl = getBaseUrl()

      // 兼容后端直接返回 /api/**
      if (url.startsWith('/api/')) {
        if (baseUrl === '/api') return url
        const hostRoot = baseUrl.endsWith('/api') ? baseUrl.slice(0, -4) : baseUrl
        return `${hostRoot}${url}`
      }

      // 兼容后端返回 /uploads/**（后端 context-path 为 /api）
      if (url.startsWith('/uploads/')) {
        return `${baseUrl}${url}`
      }

      return `${baseUrl}${url.startsWith('/') ? url : '/' + url}`
    },
    async loadComments() {
      this.loadingComments = true
      try {
        const res = await fetchCommentsByContent(this.id, { page: 1, size: 50 })
        const data = res.data || res
        const list = data.list || data.data?.list || []
        this.comments = list
        this.totalComments = data.total || data.data?.total || list.length
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '评论加载失败', icon: 'none' })
      } finally {
        this.loadingComments = false
      }
    },
    formatTime(time) {
      if (!time) return ''
      return String(time).replace('T', ' ').substring(0, 16)
    },
    previewImage(urls, idx) {
      const resolved = urls.map((u) => this.resolveMediaUrl(u))
      uni.previewImage({
        urls: resolved,
        current: resolved[idx],
      })
    },
    async sendComment() {
      if (!this.commentText.trim()) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' })
        return
      }
      this.sendingComment = true
      try {
        const payload = {
          contentId: this.id,
          commentText: this.commentText.trim(),
          parentId: 0,
        }
        const res = await createComment(payload)
        const data = res.data || res
        const comment = data.data || data
        if (comment) {
          this.comments.unshift(comment)
          this.totalComments += 1
        }
        this.commentText = ''
        uni.showToast({ title: '评论成功', icon: 'success' })
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '评论失败', icon: 'none' })
      } finally {
        this.sendingComment = false
      }
    },
  },
}
</script>

<style>
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f7;
  box-sizing: border-box;
}

.detail-scroll {
  flex: 1;
  padding: 20rpx;
  padding-bottom: 140rpx;
  box-sizing: border-box;
}

.card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #f5f5f7;
  border: 1rpx solid #e5e5ea;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.nickname {
  font-size: 32rpx;
  color: #000000;
  font-weight: 600;
  letter-spacing: -0.3rpx;
  margin-bottom: 6rpx;
}

.time {
  font-size: 24rpx;
  color: #8e8e93;
  font-weight: 400;
}

.card-content-text {
  margin-top: 20rpx;
  font-size: 30rpx;
  color: #000000;
  line-height: 1.7;
  font-weight: 400;
  letter-spacing: 0.2rpx;
}

.card-media {
  margin-top: 20rpx;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.image-item {
  width: calc((100% - 20rpx) / 3);
  height: 0;
  padding-bottom: calc((100% - 20rpx) / 3);
  border-radius: 16rpx;
  background-color: #f5f5f7;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.image-inner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-player {
  width: 100%;
  height: 420rpx;
  border-radius: 16rpx;
  background-color: #000000;
}

.tags {
  margin-top: 16rpx;
  flex-direction: row;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag-chip {
  font-size: 24rpx;
  color: #007aff;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background-color: #e5f2ff;
  font-weight: 500;
}

.meta-bar {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 0.5rpx solid #e5e5ea;
  flex-direction: row;
  display: flex;
  justify-content: space-between;
}

.meta-left {
  flex-direction: row;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.meta-chip {
  flex-direction: row;
  display: flex;
  align-items: center;
}

.meta-icon {
  font-size: 26rpx;
  color: #8e8e93;
  margin-right: 6rpx;
}

.meta-icon-star {
  color: #000000;
}

.meta-item {
  font-size: 24rpx;
  color: #8e8e93;
  font-weight: 400;
}

.meta-dot {
  margin: 0 12rpx;
  font-size: 24rpx;
  color: #c7c7cc;
}

.comment-section {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.comment-header {
  flex-direction: row;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 0.5rpx solid #e5e5ea;
}

.comment-title {
  font-size: 32rpx;
  color: #000000;
  font-weight: 600;
  letter-spacing: -0.3rpx;
}

.comment-count {
  font-size: 24rpx;
  color: #8e8e93;
  font-weight: 400;
}

.comment-empty {
  padding: 60rpx 0 40rpx;
  align-items: center;
  justify-content: center;
  display: flex;
}

.comment-empty-text {
  font-size: 26rpx;
  color: #8e8e93;
  font-weight: 400;
}

.comment-item {
  flex-direction: row;
  display: flex;
  margin-top: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 0.5rpx solid #f5f5f7;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background-color: #f5f5f7;
  border: 1rpx solid #e5e5ea;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-top-row {
  flex-direction: row;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.comment-nickname {
  font-size: 28rpx;
  color: #000000;
  font-weight: 600;
  letter-spacing: -0.2rpx;
}

.comment-time {
  font-size: 22rpx;
  color: #8e8e93;
  font-weight: 400;
}

.comment-text {
  font-size: 28rpx;
  color: #000000;
  line-height: 1.6;
  font-weight: 400;
}

/* 底部输入栏 - 固定在底部 */
.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top: 0.5rpx solid #e5e5ea;
  padding: 16rpx 20rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  z-index: 100;
}

.input-wrapper {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 20rpx;
  background-color: #f5f5f7;
  border: none;
  color: #000000;
  font-size: 28rpx;
  font-weight: 400;
}

.comment-send {
  padding: 0 28rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 20rpx;
  background-color: #000000;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
}

.comment-send:disabled {
  background-color: #c7c7cc;
  color: #ffffff;
  opacity: 0.5;
}
</style>


