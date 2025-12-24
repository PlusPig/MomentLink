<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <!-- 头部卡片：头像 + 名称 + 签名 -->
      <view class="card-header">
        <image class="avatar" :src="resolveMediaUrl(friend.avatar) || defaultAvatar" mode="aspectFill" />
        <view class="header-text">
          <text class="name">
            {{ friend.remark || friend.nickname || friend.username || '好友' }}
          </text>
          <text v-if="friend.nickname || friend.username" class="sub-name">
            {{ friend.nickname || friend.username }}
          </text>
          <text v-if="friend.signature" class="signature">
            {{ friend.signature }}
          </text>
        </view>
      </view>

      <!-- 信息列表 -->
      <view class="card">
        <view class="row" @click="editRemark">
          <view class="row-left">
            <text class="row-title">备注</text>
          </view>
          <view class="row-right">
            <text class="row-value">
              {{ friend.remark || '未设置' }}
            </text>
            <iconify-icon icon="mdi:chevron-right" class="row-chevron" />
          </view>
        </view>

        <view class="row no-arrow">
          <view class="row-left">
            <text class="row-title">状态</text>
          </view>
          <view class="row-right">
            <text class="row-tag" :class="{ blocked: friend.status === 3 }">
              {{ statusText }}
            </text>
          </view>
        </view>
      </view>

      <!-- 危险操作 -->
      <view class="card card-actions">
        <button
          class="btn btn-block"
          :class="{ 'btn-block-active': friend.status !== 3 }"
          @click="toggleBlock"
        >
          {{ friend.status === 3 ? '取消屏蔽' : '屏蔽好友' }}
        </button>
        <button class="btn btn-delete" @click="showDeleteModal">
          删除好友
        </button>
      </view>
    </scroll-view>

    <!-- 修改备注弹窗 -->
    <BottomModal :show="showRemarkModal" title="修改备注" @close="closeRemarkModal">
      <view class="remark-modal-content">
        <input
          class="remark-input"
          v-model="remarkInputValue"
          placeholder="输入新的备注名"
          placeholder-style="color:#8e8e93"
          :maxlength="50"
          @confirm="confirmUpdateRemark"
        />
        <view class="remark-modal-actions">
          <button class="remark-btn remark-btn-cancel" @click="closeRemarkModal">取消</button>
          <button class="remark-btn remark-btn-confirm" @click="confirmUpdateRemark">确定</button>
        </view>
      </view>
    </BottomModal>

    <!-- 删除确认弹窗 -->
    <BottomModal :show="showDeleteConfirmModal" title="删除好友" @close="closeDeleteModal">
      <view class="delete-modal-content">
        <text class="delete-modal-text">删除后将不再出现在好友列表中，确认删除？</text>
        <view class="delete-modal-actions">
          <button class="delete-btn delete-btn-cancel" @click="closeDeleteModal">取消</button>
          <button class="delete-btn delete-btn-confirm" @click="confirmDelete">删除</button>
        </view>
      </view>
    </BottomModal>
  </view>
</template>

<script>
import { fetchFriends, blockFriend, deleteFriend, updateRemark } from '@/api/friend'
import BottomModal from '@/components/BottomModal.vue'
import { getBaseUrl } from '@/utils/request'

export default {
  components: {
    BottomModal,
  },
  data() {
    return {
      id: null,
      friend: {},
      defaultAvatar: '/static/logo.png',
      showRemarkModal: false,
      remarkInputValue: '',
      showDeleteConfirmModal: false,
    }
  },
  computed: {
    statusText() {
      const s = this.friend.status
      if (s === 3) return '已屏蔽'
      if (s === 1) return '正常'
      if (s === 0) return '待接受'
      if (s === 2) return '已拒绝'
      return '未知'
    },
  },
  async onLoad(options) {
    this.id = Number(options.id)
    if (!this.id) {
      uni.showToast({ title: '好友不存在', icon: 'none' })
      setTimeout(() => uni.navigateBack({ delta: 1 }), 600)
      return
    }
    await this.loadFriend()
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
    async loadFriend() {
      try {
        const res = await fetchFriends()
        const data = res.data || res
        const list = data.data || data || []
        const found = list.find((f) => f.friendId === this.id)
        if (!found) {
          uni.showToast({ title: '未找到好友', icon: 'none' })
          setTimeout(() => uni.navigateBack({ delta: 1 }), 600)
          return
        }
        this.friend = found
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    editRemark() {
      this.remarkInputValue = this.friend.remark || ''
      this.showRemarkModal = true
    },
    closeRemarkModal() {
      this.showRemarkModal = false
      this.remarkInputValue = ''
    },
    async confirmUpdateRemark() {
      const value = this.remarkInputValue.trim()
      try {
        await updateRemark(this.id, value)
        this.friend.remark = value
        this.closeRemarkModal()
        uni.showToast({ title: '备注已更新', icon: 'success' })
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '修改失败', icon: 'none' })
      }
    },
    async toggleBlock() {
      try {
        await blockFriend(this.id)
        await this.loadFriend()
        uni.showToast({
          title: this.friend.status === 3 ? '已屏蔽好友' : '已取消屏蔽',
          icon: 'success',
        })
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    showDeleteModal() {
      this.showDeleteConfirmModal = true
    },
    closeDeleteModal() {
      this.showDeleteConfirmModal = false
    },
    async confirmDelete() {
      try {
        await deleteFriend(this.id)
        this.closeDeleteModal()
        uni.showToast({ title: '已删除好友', icon: 'success' })
        setTimeout(() => uni.navigateBack({ delta: 1 }), 600)
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '删除失败', icon: 'none' })
      }
    },
  },
}
</script>

<style>
.page {
  flex: 1;
  min-height: 100vh;
  background-color: #f5f5f7;
}

.scroll {
  padding: 20rpx;
}

.card-header {
  margin-bottom: 20rpx;
  padding: 48rpx 32rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  display: flex;
  flex-direction: row;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  background-color: #f5f5f7;
  border: 1rpx solid #e5e5ea;
}

.header-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.name {
  font-size: 36rpx;
  color: #000000;
  font-weight: 600;
  letter-spacing: -0.5rpx;
  margin-bottom: 8rpx;
}

.sub-name {
  font-size: 26rpx;
  color: #8e8e93;
  font-weight: 400;
  margin-bottom: 12rpx;
}

.signature {
  font-size: 28rpx;
  color: #8e8e93;
  font-weight: 400;
  line-height: 1.5;
}

.card {
  margin-top: 20rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.row {
  height: 104rpx;
  padding: 0 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  border-bottom-width: 0.5rpx;
  border-bottom-style: solid;
  border-bottom-color: #e5e5ea;
  transition: background-color 0.2s;
}

.row:active {
  background-color: #f5f5f7;
}

.row:last-child {
  border-bottom-width: 0;
}

.row-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;
}

.row-title {
  font-size: 32rpx;
  color: #000000;
  font-weight: 400;
  letter-spacing: -0.3rpx;
}

.row-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  flex: 1;
  justify-content: flex-end;
  min-width: 0;
}

.row-value {
  font-size: 28rpx;
  color: #8e8e93;
  font-weight: 400;
  flex: 1;
  min-width: 0;
  max-width: 420rpx;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-chevron {
  font-size: 24rpx;
  color: #c7c7cc;
}

.row.no-arrow {
  justify-content: space-between;
}

.row-tag {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background-color: #f5f5f7;
  color: #000000;
  font-weight: 500;
}

.row-tag.blocked {
  background-color: #fee2e2;
  color: #ff3b30;
}

.card-actions {
  margin-top: 20rpx;
  padding: 16rpx 24rpx 24rpx;
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}

.card-actions .btn {
  flex: 1;
  min-width: 0;
}

.btn {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  letter-spacing: -0.3rpx;
}

.btn-block {
  background-color: #f5f5f7;
  color: #000000;
}

.btn-block:active {
  background-color: #e5e5ea;
}

.btn-block-active {
  background-color: #000000;
  color: #ffffff;
}

.btn-block-active:active {
  background-color: #333333;
}

.btn-delete {
  background-color: #ffffff;
  color: #ff3b30;
  border: 0.5rpx solid #ff3b30;
}

.btn-delete:active {
  background-color: rgba(255, 59, 48, 0.1);
}

/* 修改备注弹窗样式 */
.remark-modal-content {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.remark-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background-color: #f5f5f7;
  border: none;
  color: #000000;
  font-size: 30rpx;
  font-weight: 400;
  box-sizing: border-box;
}

.remark-modal-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}

.remark-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
}

.remark-btn-cancel {
  background-color: #f5f5f7;
  color: #000000;
}

.remark-btn-cancel:active {
  background-color: #e5e5ea;
}

.remark-btn-confirm {
  background-color: #000000;
  color: #ffffff;
}

.remark-btn-confirm:active {
  background-color: #333333;
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



