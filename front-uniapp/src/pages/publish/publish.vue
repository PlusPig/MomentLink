<template>
  <view class="publish-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="nav-action" @click="onCancel">取消</text>

      <view class="nav-center-placeholder"></view>

      <button class="nav-submit" :loading="submitting" @click="onSubmit">
        <iconify-icon icon="mdi:send" class="nav-submit-icon" />
        <text class="nav-submit-text">发表</text>
      </button>

    </view>

    <!-- 主体 -->
    <scroll-view scroll-y class="body-scroll">
      <!-- 输入 + 媒体 -->
      <view class="card-main">
        <textarea
          class="textarea"
          v-model="form.contentText"
          placeholder="分享新鲜事..."
          placeholder-style="color:#c0c4cc"
          :maxlength="500"
          auto-height
        />

        <!-- 照片/视频 + 功能按钮 -->
        <view class="bottom-row">
          <!-- 照片/视频选择框 -->
          <view class="media-picker" @click="chooseMedia">
            <iconify-icon icon="mdi:image-outline" class="media-icon" />
            <text class="media-text">照片/视频</text>
          </view>

          <!-- 功能按钮区：紧挨在照片/视频下方，单排排列 -->
          <view class="tools-row">
            <view class="tool-pill">
              <iconify-icon icon="mdi:at" class="tool-icon" />
              <text class="tool-text">好友</text>
            </view>
            <view class="tool-pill" @click.stop="onAddLocation">
              <iconify-icon icon="mdi:map-marker-outline" class="tool-icon" />
              <text class="tool-text">添加地点</text>
            </view>
            <view class="tool-pill" @click.stop="onAddTag">
              <iconify-icon icon="mdi:tag-outline" class="tool-icon" />
              <text class="tool-text">添加标签</text>
            </view>
            <view class="tool-pill tool-pill-ai" @click.stop>
              <iconify-icon icon="mdi:sparkles" class="tool-icon" />
              <text class="tool-text">AI配文</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 已选媒体预览 -->
      <view v-if="mediaFiles.length" class="media-preview">
        <view
          v-for="(file, index) in mediaFiles"
          :key="index"
          class="media-thumb-wrap"
        >
          <image
            v-if="form.type === 1"
            class="media-thumb"
            :src="file.path"
            mode="aspectFill"
          />
          <view v-else class="media-video-thumb">
            <iconify-icon icon="mdi:play-circle-outline" class="media-video-icon" />
          </view>
          <view class="media-remove" @click.stop="removeMedia(index)">×</view>
        </view>
      </view>

      <!-- 权限设置卡片 -->
      <view class="card-permission">
        <view class="perm-left">
          <iconify-icon icon="mdi:eye-outline" class="perm-icon" />
          <text class="perm-title">权限设置</text>
        </view>
        <view class="perm-right">
          <text class="perm-value">所有人可见</text>
          <iconify-icon icon="mdi:chevron-right" class="perm-arrow" />
        </view>
      </view>
    </scroll-view>

    <!-- 添加标签弹窗 -->
    <BottomModal :show="showTagModal" title="添加标签" @close="closeTagModal">
      <view class="tag-modal-content">
        <input
          class="tag-input"
          v-model="tagInputValue"
          placeholder="例如：旅行、日常"
          placeholder-style="color:#8e8e93"
          :maxlength="20"
          @confirm="confirmAddTag"
        />
        <view class="tag-modal-actions">
          <button class="tag-btn tag-btn-cancel" @click="closeTagModal">取消</button>
          <button class="tag-btn tag-btn-confirm" @click="confirmAddTag">确定</button>
        </view>
      </view>
    </BottomModal>
  </view>
</template>

<script>
import { getBaseUrl } from '@/utils/request'
import BottomModal from '@/components/BottomModal.vue'

export default {
  components: {
    BottomModal,
  },
  data() {
    return {
      form: {
        type: 1, // 1: 图片 2: 视频 3: 文本
        contentText: '',
      },
      mediaFiles: [],
      maxMediaCount: 9,
      submitting: false,
      showTagModal: false,
      tagInputValue: '',
    }
  },
  computed: {
    parsedTags() {
      const text = this.form.contentText || ''
      const matches = text.match(/#([^\s#]+)/g) || []
      const names = matches.map((m) => m.replace('#', '').trim()).filter(Boolean)
      // 去重
      return [...new Set(names)]
    },
  },
  methods: {
    chooseMedia() {
      // 默认按照是否已有视频判断：已选视频 -> 只能重新选视频；否则先选图片
      if (this.form.type === 2) {
        this.chooseVideo()
      } else {
        this.chooseImage()
      }
    },
    chooseImage() {
      this.form.type = 1
      const remain = this.maxMediaCount - this.mediaFiles.length
      if (remain <= 0) {
        uni.showToast({ title: '最多选择 9 张图片', icon: 'none' })
        return
      }
      uni.chooseImage({
        count: remain,
        sizeType: ['compressed'],
        success: (res) => {
          const files = res.tempFilePaths.map((path) => ({ path }))
          this.mediaFiles = this.mediaFiles.concat(files)
        },
      })
    },
    chooseVideo() {
      this.form.type = 2
      if (this.mediaFiles.length >= 1) {
        uni.showToast({ title: '视频暂时仅支持 1 个', icon: 'none' })
        return
      }
      uni.chooseVideo({
        compressed: true,
        success: (res) => {
          this.mediaFiles = [{ path: res.tempFilePath }]
        },
      })
    },
    removeMedia(index) {
      this.mediaFiles.splice(index, 1)
      if (!this.mediaFiles.length) {
        this.form.type = 3
      }
    },
    onCancel() {
      uni.navigateBack({ delta: 1 })
    },
    onAddLocation() {
      uni.showToast({ title: '地点暂未接后端，当前仅作展示', icon: 'none' })
    },
    onAddTag() {
      this.tagInputValue = ''
      this.showTagModal = true
    },
    closeTagModal() {
      this.showTagModal = false
      this.tagInputValue = ''
    },
    confirmAddTag() {
      const value = this.tagInputValue.trim()
      if (!value) {
        uni.showToast({ title: '请输入标签', icon: 'none' })
        return
      }
      const tagText = value.startsWith('#') ? value : `#${value}`
      let content = this.form.contentText || ''
      if (content && !content.endsWith(' ') && !content.endsWith('\n')) {
        content += ' '
      }
      this.form.contentText = `${content}${tagText} `
      this.closeTagModal()
    },
    async onSubmit() {
      if (!this.form.contentText && this.mediaFiles.length === 0) {
        uni.showToast({ title: '写点内容或选择媒体吧', icon: 'none' })
        return
      }

      // 自动判断类型：有视频 -> 2，有图片 -> 1，否则 3 文本
      if (this.mediaFiles.length === 0) {
        this.form.type = 3
      }

      this.submitting = true
      const baseUrl = getBaseUrl()
      const token = uni.getStorageSync('authToken') || ''

      try {
        await new Promise((resolve, reject) => {
          uni.uploadFile({
            url: `${baseUrl}/contents`,
            header: {
              Authorization: token,
            },
            formData: {
              type: this.form.type,
              contentText: this.form.contentText || '',
              tags: this.parsedTags.join(','),
            },
            files: this.mediaFiles.map((file) => ({
              name: 'files',
              uri: file.path,
            })),
            success: (res) => {
              try {
                const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
                if (data && data.code === 200) {
                  resolve(data)
                } else {
                  reject(new Error(data.message || '发布失败'))
                }
              } catch (e) {
                reject(e)
              }
            },
            fail: (err) => {
              reject(err)
            },
          })
        })

        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack({ delta: 1 })
        }, 600)
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '发布失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },
  },
}
</script>

<style>
.publish-page {
  flex: 1;
  min-height: 100vh;
  background-color: #f5f5f7;
  box-sizing: border-box;
  overflow-x: hidden;
}

.nav-bar {
  height: 88rpx;
  padding: 0 20rpx;
  display: flex;
  flex-direction: row;
  align-items: right;
  justify-content: space-between;
  background-color: #ffffff;
  border-bottom: 0.5rpx solid #e5e5ea;
}

.nav-action {
  font-size: 32rpx;
  color: #000000;
  font-weight: 400;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #000000;
  letter-spacing: -0.3rpx;
}

.nav-submit {
  font-size: 28rpx;
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  background-color: #000000;
  color: #ffffff;
  display: flex;
  flex-direction: row;
  align-items: center;
  font-weight: 600;
  border: none;
}

.nav-submit-icon {
  font-size: 28rpx;
  margin-right: 6rpx;
}

.nav-submit-text {
  font-size: 28rpx;
}

.body-scroll {
  padding: 20rpx;
  box-sizing: border-box;
}

.card-main {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}

.textarea {
  min-height: 240rpx;
  width: 100%;
  font-size: 30rpx;
  color: #000000;
  line-height: 1.7;
  font-weight: 400;
  letter-spacing: 0.2rpx;
}

.bottom-row {
  margin-top: 28rpx;
  flex-direction: column;
  display: flex;
  align-items: flex-start;
}

.media-picker {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  border: 1rpx dashed #c7c7cc;
  background-color: #f5f5f7;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.media-picker:active {
  background-color: #e5e5ea;
}

.media-icon {
  font-size: 48rpx;
  color: #8e8e93;
}

.media-text {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #8e8e93;
  font-weight: 400;
}

.tools-row {
  margin-top: 20rpx;
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 12rpx;
  box-sizing: border-box;
}

.tool-pill {
  height: 64rpx;
  padding: 0 20rpx;
  border-radius: 20rpx;
  background-color: #f5f5f7;
  display: flex;
  flex-direction: row;
  align-items: center;
  transition: all 0.2s;
}

.tool-pill:active {
  background-color: #e5e5ea;
}

.tool-pill-ai {
  background-color: #f5f5f7;
}

.tool-icon {
  font-size: 28rpx;
  color: #000000;
  margin-right: 8rpx;
}

.tool-pill-ai .tool-icon {
  color: #000000;
}

.tool-text {
  font-size: 26rpx;
  color: #000000;
  font-weight: 500;
}

.media-preview {
  margin-top: 20rpx;
  padding: 0;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
  box-sizing: border-box;
}

.media-thumb-wrap {
  position: relative;
  width: calc((100% - 24rpx) / 3);
  height: 0;
  padding-bottom: calc((100% - 24rpx) / 3);
  border-radius: 16rpx;
  overflow: hidden;
  background-color: #f5f5f7;
  box-sizing: border-box;
}

.media-thumb {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-video-thumb {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000000;
}

.media-video-icon {
  font-size: 64rpx;
  color: #ffffff;
}

.media-remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  font-size: 28rpx;
  text-align: center;
  line-height: 40rpx;
  font-weight: 500;
  backdrop-filter: blur(10rpx);
}

.card-permission {
  margin-top: 20rpx;
  padding: 0 24rpx;
  height: 104rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.perm-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.perm-icon {
  font-size: 32rpx;
  color: #000000;
  margin-right: 16rpx;
}

.perm-title {
  font-size: 32rpx;
  color: #000000;
  font-weight: 400;
  letter-spacing: -0.3rpx;
}

.perm-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.perm-value {
  font-size: 28rpx;
  color: #8e8e93;
  font-weight: 400;
}

.perm-arrow {
  font-size: 24rpx;
  color: #c7c7cc;
}

/* 添加标签弹窗样式 */
.tag-modal-content {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.tag-input {
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

.tag-modal-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}

.tag-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
}

.tag-btn-cancel {
  background-color: #f5f5f7;
  color: #000000;
}

.tag-btn-cancel:active {
  background-color: #e5e5ea;
}

.tag-btn-confirm {
  background-color: #000000;
  color: #ffffff;
}

.tag-btn-confirm:active {
  background-color: #333333;
}
</style>

