<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav-bar">
      <view class="nav-content">
        <text class="nav-title">我的动态</text>
      </view>
    </view>

    <!-- 搜索栏 - 固定在导航栏下方 -->
    <view class="search-bar">
      <view class="search-tabs">
        <view
          class="tab-item"
          :class="{ active: searchType === 'date' }"
          @click="searchType = 'date'"
        >
          <iconify-icon icon="mdi:calendar-outline" class="tab-icon" />
          <text class="tab-text">按日期</text>
        </view>
        <view
          class="tab-item"
          :class="{ active: searchType === 'tag' }"
          @click="searchType = 'tag'"
        >
          <iconify-icon icon="mdi:tag-outline" class="tab-icon" />
          <text class="tab-text">按标签</text>
        </view>
      </view>

      <view v-if="searchType === 'date'" class="date-picker-row">
        <view class="date-picker-left" @click="openStartDatePicker">
          <text class="date-label">开始日期</text>
          <text class="date-value">{{ startDate || '选择日期' }}</text>
        </view>
        <text class="date-sep">至</text>
        <view class="date-picker-right" @click="openEndDatePicker">
          <text class="date-label">结束日期</text>
          <text class="date-value">{{ endDate || '选择日期' }}</text>
        </view>
      </view>

      <view v-if="searchType === 'tag'" class="tag-input-row">
        <input
          class="tag-input"
          v-model="tagKeyword"
          placeholder="输入标签关键词"
          placeholder-style="color:#8e8e93"
          @confirm="handleSearch"
        />
        <button class="btn-search" @click="handleSearch">搜索</button>
      </view>
    </view>

    <!-- 内容列表 -->
    <scroll-view
      scroll-y
      class="list-scroll"
      refresher-enabled
      :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="contents.length === 0 && !loading" class="empty">
        <text class="empty-text">还没有内容</text>
      </view>

      <view
        v-for="item in contents"
        :key="item.id"
        class="card"
      >
        <view class="card-header">
          <text class="card-time">{{ formatTime(item.createdAt) }}</text>
          <view class="card-actions">
            <button class="action-btn action-btn-edit" @click.stop="goEdit(item.id)">
              <iconify-icon icon="mdi:pencil-outline" class="action-icon" />
              <text class="action-text">编辑</text>
            </button>
            <button class="action-btn action-btn-delete" @click.stop="showDeleteModal(item)">
              <iconify-icon icon="mdi:delete-outline" class="action-icon" />
              <text class="action-text">删除</text>
            </button>
          </view>
        </view>

        <view v-if="item.contentText" class="card-text">{{ item.contentText }}</view>

        <view v-if="item.mediaUrls && item.mediaUrls.length" class="card-media">
          <image
            v-if="item.type === 1"
            class="media-thumb"
            :src="resolveMediaUrl(item.mediaUrls[0])"
            mode="aspectFill"
          />
          <view v-else-if="item.type === 2" class="video-badge">
            <iconify-icon icon="mdi:play-circle-outline" class="video-icon" />
          </view>
        </view>

        <view v-if="item.tags && item.tags.length" class="card-tags">
          <text v-for="(tag, idx) in item.tags" :key="idx" class="tag-chip">#{{ tag }}</text>
        </view>
      </view>

      <view v-if="loadingMore" class="load-more">
        <text class="load-more-text">加载中...</text>
      </view>
      <view v-else-if="!hasMore && contents.length > 0" class="load-more">
        <text class="load-more-text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 删除确认弹窗 -->
    <BottomModal :show="showDeleteConfirmModal" title="删除动态" @close="closeDeleteModal">
      <view class="delete-modal-content">
        <text class="delete-modal-text">确定要删除这条动态吗？</text>
        <view class="delete-modal-actions">
          <button class="delete-btn delete-btn-cancel" @click="closeDeleteModal">取消</button>
          <button class="delete-btn delete-btn-confirm" @click="confirmDelete">删除</button>
        </view>
      </view>
    </BottomModal>
  </view>
</template>

<script>
import { fetchMyContents, deleteContent } from '@/api/user'
import { getBaseUrl } from '@/utils/request'
import BottomModal from '@/components/BottomModal.vue'

export default {
  components: {
    BottomModal,
  },
  data() {
    return {
      contents: [],
      page: 1,
      size: 10,
      hasMore: true,
      loading: false,
      loadingMore: false,
      refresherTriggered: false,
      searchType: 'date', // 'date' | 'tag'
      startDate: '',
      endDate: '',
      tagKeyword: '',
      showDeleteConfirmModal: false,
      deleteItem: null,
    }
  },
  onLoad() {
    this.loadContents()
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
    async loadContents() {
      if (this.loading || this.loadingMore) return
      if (!this.hasMore && this.page > 1) return

      if (this.page === 1) {
        this.loading = true
      } else {
        this.loadingMore = true
      }

      try {
        const params = {
          page: this.page,
          size: this.size,
        }
        if (this.searchType === 'date' && this.startDate) {
          params.startDate = this.startDate
        }
        if (this.searchType === 'date' && this.endDate) {
          params.endDate = this.endDate
        }
        if (this.searchType === 'tag' && this.tagKeyword) {
          params.tags = this.tagKeyword
        }

        const res = await fetchMyContents(params)
        const data = res.data || res
        const list = (data.list || data.data?.list || []).map(this.normalizeItem)

        if (this.page === 1) {
          this.contents = list
        } else {
          this.contents = this.contents.concat(list)
        }

        const total = data.total || data.data?.total || 0
        this.hasMore = this.page * this.size < total
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '加载失败', icon: 'none' })
      } finally {
        this.loading = false
        this.loadingMore = false
        this.refresherTriggered = false
      }
    },
    normalizeItem(item) {
      if (item && typeof item.tags === 'string') {
        item.tags = item.tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean)
      }
      return item
    },
    formatTime(time) {
      if (!time) return ''
      return String(time).replace('T', ' ').substring(0, 16)
    },
    onStartDateChange(e) {
      const value = e.detail.value || e.detail
      console.log('开始日期选择:', value, e)
      if (value) {
        this.startDate = value
        this.page = 1
        this.hasMore = true
        this.loadContents()
      }
    },
    onEndDateChange(e) {
      const value = e.detail.value || e.detail
      console.log('结束日期选择:', value, e)
      if (value) {
        this.endDate = value
        this.page = 1
        this.hasMore = true
        this.loadContents()
      }
    },
    handleSearch() {
      this.page = 1
      this.hasMore = true
      this.loadContents()
    },
    onRefresh() {
      this.refresherTriggered = true
      this.page = 1
      this.hasMore = true
      this.loadContents()
    },
    loadMore() {
      if (!this.hasMore || this.loadingMore) return
      this.page++
      this.loadContents()
    },
    goEdit(id) {
      uni.navigateTo({ url: `/pages/profile/content-edit?id=${id}` })
    },
    showDeleteModal(item) {
      this.deleteItem = item
      this.showDeleteConfirmModal = true
    },
    closeDeleteModal() {
      this.showDeleteConfirmModal = false
      this.deleteItem = null
    },
    async confirmDelete() {
      if (!this.deleteItem) return
      try {
        await deleteContent(this.deleteItem.id)
        this.closeDeleteModal()
        uni.showToast({ title: '删除成功', icon: 'success' })
        this.contents = this.contents.filter((c) => c.id !== this.deleteItem.id)
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败', icon: 'none' })
      }
    },
  },
}
</script>

<style>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f7;
}

/* 自定义导航栏 */
.custom-nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 200;
  height: calc(44px + env(safe-area-inset-top));
  padding-top: env(safe-area-inset-top);
  background-color: #ffffff;
  border-bottom: 0.5rpx solid #e5e5ea;
  box-sizing: border-box;
}

.nav-content {
  height: 44px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  padding: 0 20rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.search-bar {
  position: fixed;
  top: calc(44px + env(safe-area-inset-top));
  left: 0;
  right: 0;
  z-index: 100;
  padding: 24rpx 20rpx;
  background-color: #ffffff;
  border-bottom: 0.5rpx solid #e5e5ea;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}

/* 计算筛选栏高度：标签(60rpx) + 间距(20rpx) + 日期选择器(72rpx) + padding(48rpx) = 200rpx */

.search-tabs {
  display: flex;
  flex-direction: row;
  margin-bottom: 20rpx;
  background-color: #f5f5f7;
  border-radius: 16rpx;
  padding: 4rpx;
}

.tab-item {
  flex: 1;
  padding: 12rpx 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  transition: all 0.2s;
}

.tab-item.active {
  background-color: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.tab-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
  color: #8e8e93;
}

.tab-item.active .tab-icon {
  color: #000000;
}

.tab-text {
  font-size: 26rpx;
  color: #8e8e93;
  font-weight: 500;
}

.tab-item.active .tab-text {
  color: #000000;
  font-weight: 600;
}

.date-picker-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.date-picker-left, .date-picker-right {
  flex: 1;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background-color: #f5f5f7;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.date-label {
  font-size: 22rpx;
  color: #8e8e93;
  margin-bottom: 6rpx;
  font-weight: 400;
}

.date-value {
  font-size: 28rpx;
  color: #000000;
  font-weight: 500;
}

.date-sep {
  font-size: 26rpx;
  color: #8e8e93;
  font-weight: 400;
  padding: 0 16rpx;
}

.tag-input-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.tag-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background-color: #f5f5f7;
  font-size: 28rpx;
  color: #000000;
}

.btn-search {
  padding: 0 32rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 16rpx;
  background-color: #000000;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
}

.list-scroll {
  flex: 1;
  padding: 20rpx;
  padding-top: calc(44px + env(safe-area-inset-top) + 200rpx);
  box-sizing: border-box;
  margin-top: 30px;
}

.empty {
  padding: 160rpx 0;
  display: flex;
  justify-content: center;
}

.empty-text {
  font-size: 28rpx;
  color: #8e8e93;
  font-weight: 400;
}

.card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.card-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  gap: 12rpx;
  min-width: 0;
}

.card-time {
  font-size: 24rpx;
  color: #8e8e93;
  font-weight: 400;
  flex-shrink: 0;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  flex-shrink: 0;
}

.action-btn {
  height: 56rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
  font-size: 26rpx;
  font-weight: 500;
  border: none;
  transition: all 0.2s;
}

.action-btn-edit {
  background-color: #f5f5f7;
  color: #000000;
}

.action-btn-edit:active {
  background-color: #e5e5ea;
}

.action-btn-delete {
  background-color: #fff5f5;
  color: #ff3b30;
}

.action-btn-delete:active {
  background-color: #ffe5e5;
}

.action-icon {
  font-size: 24rpx;
}

.action-text {
  font-size: 26rpx;
  font-weight: 500;
}

.card-text {
  font-size: 30rpx;
  color: #000000;
  line-height: 1.6;
  margin-bottom: 16rpx;
  font-weight: 400;
  word-wrap: break-word;
  word-break: break-all;
}

.card-media {
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.media-thumb {
  width: 100%;
  max-width: 100%;
  height: 400rpx;
  border-radius: 16rpx;
  background-color: #f5f5f7;
  box-sizing: border-box;
}

.video-badge {
  width: 100%;
  max-width: 100%;
  height: 400rpx;
  border-radius: 16rpx;
  background-color: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.video-icon {
  font-size: 80rpx;
  color: #8e8e93;
}

.card-tags {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag-chip {
  font-size: 24rpx;
  color: #007aff;
  background-color: #e5f2ff;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.load-more {
  padding: 32rpx 0;
  display: flex;
  justify-content: center;
}

.load-more-text {
  font-size: 26rpx;
  color: #8e8e93;
  font-weight: 400;
}

/* 删除确认弹窗样式 */
.delete-modal-content {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.delete-modal-text {
  font-size: 30rpx;
  color: #000000;
  font-weight: 400;
  line-height: 1.6;
  text-align: center;
  padding: 0 20rpx;
}

.delete-modal-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}

.delete-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
}

.delete-btn-cancel {
  background-color: #f5f5f7;
  color: #000000;
}

.delete-btn-cancel:active {
  background-color: #e5e5ea;
}

.delete-btn-confirm {
  background-color: #ff3b30;
  color: #ffffff;
}

.delete-btn-confirm:active {
  background-color: #ff1f14;
}
</style>

