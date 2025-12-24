<template>
  <view class="page">
    <!-- 顶部栏 -->
    <view class="nav-bar">
      <view class="nav-left">
        <text class="app-title">MomentLink | 不止瞬间</text>
      </view>
      <view class="nav-right">
        <view class="publish-btn-icon" @click="goToPublish">
          <iconify-icon icon="mdi:plus" class="publish-icon" />
        </view>
      </view>
    </view>

    <!-- 内容列表 -->
    <scroll-view
      class="feed"
      scroll-y
      refresher-enabled
      :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="contents.length === 0 && !loading" class="empty">
        <text class="empty-text">还没有内容，去发布第一条动态吧～</text>
      </view>

      <view
        v-for="item in contents"
        :key="item.id"
        class="card"
        @click="goToDetail(item.id)"
      >
        <!-- 头部：头像 + 昵称 + 时间 -->
        <view class="card-header">
          <image
            class="avatar"
            :src="resolveMediaUrl(item.avatar) || defaultAvatar"
            mode="aspectFill"
          />
          <view class="header-info">
            <text class="nickname">{{ item.nickname || item.username || '用户' }}</text>
            <text class="time">{{ formatTime(item.createdAt) }}</text>
          </view>
        </view>

        <!-- 文本内容 -->
        <view v-if="item.contentText" class="card-content-text">
          <text>{{ item.contentText }}</text>
        </view>

        <!-- 媒体内容：图片 / 视频 -->
        <view v-if="item.mediaUrls && item.mediaUrls.length" class="card-media">
          <block v-if="item.type === 1">
            <!-- 图片九宫格 -->
            <view class="image-grid">
              <view
                v-for="(url, idx) in item.mediaUrls"
                :key="idx"
                class="image-item"
                @click.stop="previewImage(item.mediaUrls, idx)"
              >
                <image class="image-inner" :src="resolveMediaUrl(url)" mode="aspectFill" />
              </view>
            </view>
          </block>
          <block v-else-if="item.type === 2">
            <!-- 简单视频封面/播放器 -->
            <video
              class="video-player"
              :src="resolveMediaUrl(item.mediaUrls[0])"
              controls
              @click.stop
            />
          </block>
        </view>

        <!-- 标签 -->
        <view v-if="item.tags && item.tags.length" class="tags">
          <text v-for="(tag, idx) in item.tags" :key="idx" class="tag-chip">
            #{{ tag }}
          </text>
        </view>

        <!-- 底部操作栏：点赞 / 评论 / 评分 -->
        <view class="card-footer" @click.stop>
          <view class="footer-left">
            <view class="footer-item" @click="toggleLike(item)">
              <iconify-icon
                icon="mdi:heart-outline"
                class="footer-icon"
                :class="{ liked: item.isLiked }"
              />
              <text class="footer-text">{{ item.likeCount || 0 }}</text>
            </view>
            <view class="footer-item" @click="goToDetail(item.id)">
              <iconify-icon icon="mdi:comment-outline" class="footer-icon" />
              <text class="footer-text">{{ item.commentCount || 0 }}</text>
            </view>
          </view>
          <view class="footer-right">
            <view class="rating" @click="openRating(item)">
              <view class="stars">
                <iconify-icon
                  v-for="i in 5"
                  :key="i"
                  :icon="i <= Math.round(item.avgRating || 0) ? 'mdi:star' : 'mdi:star-outline'"
                  class="star-icon"
                  :class="{ filled: i <= Math.round(item.avgRating || 0) }"
                />
              </view>
              <text class="rating-score">{{ (item.avgRating || 0).toFixed(1) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 图片预览弹窗 -->
      <ImagePreview
        :show="showImagePreview"
        :images="previewImages"
        :current="previewCurrentIndex"
        @close="closeImagePreview"
      />

      <!-- 评价弹窗 -->
      <BottomModal :show="showRatingModal" title="评价" @close="closeRatingModal">
        <view class="rating-stars-row">
          <view
            v-for="i in 5"
            :key="i"
            class="rating-star-btn"
            @click="selectRating(i)"
          >
            <iconify-icon
              :icon="i <= ratingSelected ? 'mdi:star' : 'mdi:star-outline'"
              class="rating-star-icon"
              :class="{ filled: i <= ratingSelected }"
            />
          </view>
        </view>
        <button class="rating-submit-btn" @click="submitRating">确认</button>
      </BottomModal>

      <!-- 加载更多 -->
      <view v-if="loadingMore" class="load-more">
        <text class="load-more-text">加载中...</text>
      </view>
      <view v-else-if="!hasMore && contents.length > 0" class="load-more">
        <text class="load-more-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { fetchContents, likeContent, unlikeContent, rateContentOnContent } from '@/api/content'
import { getBaseUrl } from '@/utils/request'
import BottomModal from '@/components/BottomModal.vue'
import ImagePreview from '@/components/ImagePreview.vue'

export default {
  components: {
    BottomModal,
    ImagePreview,
  },
  data() {
    return {
      user: null,
      contents: [],
      page: 1,
      size: 10,
      hasMore: true,
      loading: false,
      loadingMore: false,
      refresherTriggered: false,
      defaultAvatar: '/static/logo.png',
      showImagePreview: false,
      previewImages: [],
      previewCurrentIndex: 0,
      showRatingModal: false,
      ratingItem: null,
      ratingSelected: 0,
    }
  },
  onShow() {
    const authToken = uni.getStorageSync('authToken')
    if (!authToken) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.user = uni.getStorageSync('user') || null

    // 每次进入首页尝试刷新第一页
    this.refresh()
  },
  methods: {
    async refresh() {
      this.page = 1
      this.hasMore = true
      this.loading = true
      try {
        const res = await fetchContents({ page: this.page, size: this.size })
        const data = res.data || res
        this.contents = (data.list || data.data?.list || []).map(this.normalizeItem)
        const total = data.total || data.data?.total || 0
        this.hasMore = this.page * this.size < total
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '加载失败', icon: 'none' })
      } finally {
        this.loading = false
        this.refresherTriggered = false
      }
    },
    async loadMore() {
      if (!this.hasMore || this.loadingMore || this.loading) return
      this.loadingMore = true
      try {
        const nextPage = this.page + 1
        const res = await fetchContents({ page: nextPage, size: this.size })
        const data = res.data || res
        const list = (data.list || data.data?.list || []).map(this.normalizeItem)
        if (list.length > 0) {
          this.page = nextPage
          this.contents = this.contents.concat(list)
        }
        const total = data.total || data.data?.total || 0
        this.hasMore = this.page * this.size < total
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '加载更多失败', icon: 'none' })
      } finally {
        this.loadingMore = false
      }
    },
    onRefresh() {
      this.refresherTriggered = true
      this.refresh()
    },
    normalizeItem(item) {
      // 确保 tags 为数组
      if (item && typeof item.tags === 'string') {
        item.tags = item.tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean)
      }
      return item
    },
    resolveMediaUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      // 后端返回 /uploads/**，需要拼上 /api 前缀
      // 如果 url 已经是 /api/uploads/**，直接返回
      if (url.startsWith('/api/')) return url
      // 如果 url 是 /uploads/**，拼上 baseUrl
      if (url.startsWith('/uploads/')) {
        return `${getBaseUrl()}${url}`
      }
      // 其他情况，拼上 baseUrl
      return `${getBaseUrl()}${url.startsWith('/') ? url : '/' + url}`
    },
    formatTime(time) {
      if (!time) return ''
      // 简单展示，后续可接入 dayjs 等库
      return String(time).replace('T', ' ').substring(0, 16)
    },
    previewImage(urls, current) {
      this.previewImages = urls
      this.previewCurrentIndex = current
      this.showImagePreview = true
    },
    closeImagePreview() {
      this.showImagePreview = false
      setTimeout(() => {
        this.previewImages = []
        this.previewCurrentIndex = 0
      }, 300)
    },
    async toggleLike(item) {
      if (!item || !item.id) return
      const originalLiked = !!item.isLiked
      const originalCount = item.likeCount || 0

      // 前端先乐观更新
      item.isLiked = !originalLiked
      item.likeCount = originalLiked
        ? Math.max(0, originalCount - 1)
        : originalCount + 1

      try {
        if (!originalLiked) {
          await likeContent(item.id)
        } else {
          await unlikeContent(item.id)
        }
      } catch (e) {
        // 失败回滚
        item.isLiked = originalLiked
        item.likeCount = originalCount
        uni.showToast({ title: e.message || '操作失败', icon: 'none' })
      }
    },
    openRating(item) {
      if (!item) return
      this.ratingItem = item
      this.ratingSelected = Math.round(item.avgRating || 0)
      this.showRatingModal = true
    },
    closeRatingModal() {
      this.showRatingModal = false
      setTimeout(() => {
        this.ratingItem = null
        this.ratingSelected = 0
      }, 300)
    },
    selectRating(score) {
      this.ratingSelected = score
    },
    async submitRating() {
      if (!this.ratingItem || !this.ratingSelected) return
      try {
        await rateContentOnContent(this.ratingItem.id, this.ratingSelected)
        uni.showToast({ title: '评分成功', icon: 'success' })
        this.ratingItem.avgRating = this.ratingSelected
        this.closeRatingModal()
      } catch (e) {
        uni.showToast({ title: '评分失败', icon: 'none' })
        console.error(e)
      }
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
    },
    goToPublish() {
      uni.navigateTo({ url: '/pages/publish/publish' })
    },
  },
}
</script>

<style>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #ffffff;
  box-sizing: border-box;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  z-index: 100;
}

.app-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.publish-btn-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: #000000;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.publish-icon {
  font-size: 32rpx;
  color: #ffffff;
}

.feed {
  flex: 1;
  padding-top: 96rpx;
  padding-bottom: 40rpx;
  box-sizing: border-box;
}

.empty {
  padding: 120rpx 0;
  align-items: center;
  justify-content: center;
  display: flex;
}

.empty-text {
  font-size: 28rpx;
  color: #6b7280;
}

.card {
  background-color: #ffffff;
  padding: 32rpx;
  margin-bottom: 1px;
  border-bottom: 1px solid #f3f4f6;
  box-sizing: border-box;
}

.card:last-child {
  border-bottom: none;
}

.card-header {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
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
  letter-spacing: 0.2rpx;
}

.card-media {
  margin-top: 16rpx;
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
  border-radius: 12rpx;
  background-color: #f3f4f6;
  box-sizing: border-box;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
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
  border-radius: 12rpx;
  background-color: #000000;
}

.tags {
  margin-top: 12rpx;
  flex-direction: row;
  flex-wrap: wrap;
  display: flex;
  gap: 10rpx;
}

.tag-chip {
  font-size: 24rpx;
  color: #007aff;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background-color: #e5f2ff;
  font-weight: 500;
}

.card-footer {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1px solid #f3f4f6;
  flex-direction: row;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-left {
  flex-direction: row;
  display: flex;
}

.footer-item {
  flex-direction: row;
  display: flex;
  align-items: center;
  margin-right: 24rpx;
}

.footer-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
  color: #000000;
}

.footer-icon.liked {
  color: #ef4444;
}

.footer-text {
  font-size: 26rpx;
  color: #000000;
  font-weight: 500;
}

.footer-right {
  flex-direction: row;
  display: flex;
  align-items: center;
}

.rating {
  flex-direction: row;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stars {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
}

.star-icon {
  font-size: 24rpx;
  color: #d1d5db;
}

.star-icon.filled {
  color: #000000;
}

.rating-score {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 500;
}

.load-more {
  padding: 20rpx 0;
  display: flex;
  justify-content: center;
}

.load-more-text {
  font-size: 24rpx;
  color: #6b7280;
}


/* 评价弹窗内容样式 */
.rating-stars-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  margin-bottom: 40rpx;
}

.rating-star-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rating-star-icon {
  font-size: 64rpx;
  color: #d1d5db;
  transition: all 0.2s ease;
}

.rating-star-icon.filled {
  color: #000000;
}

.rating-submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  background-color: #000000;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}
</style>
