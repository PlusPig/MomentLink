<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="section">
        <view class="cell">
          <text class="cell-label">内容</text>
          <textarea
            class="cell-textarea"
            v-model="form.contentText"
            placeholder="编辑内容..."
            placeholder-style="color:#9ca3af"
            :maxlength="500"
            auto-height
          />
        </view>
        <view class="cell">
          <text class="cell-label">标签</text>
          <input
            class="cell-input"
            v-model="tagsInput"
            placeholder="用逗号分隔"
            placeholder-style="color:#9ca3af"
          />
        </view>
        <view v-if="parsedTags.length" class="tags-preview">
          <text v-for="(tag, idx) in parsedTags" :key="idx" class="tag-chip">#{{ tag }}</text>
        </view>
      </view>

      <view class="actions">
        <button class="btn-save" :loading="saving" @click="handleSave">保存</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { fetchContentDetail } from '@/api/content'
import { updateContent } from '@/api/user'

export default {
  data() {
    return {
      id: null,
      form: {
        contentText: '',
        tags: '',
      },
      tagsInput: '',
      saving: false,
    }
  },
  computed: {
    parsedTags() {
      if (!this.tagsInput) return []
      return this.tagsInput
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean)
    },
  },
  onLoad(options) {
    this.id = Number(options.id)
    if (!this.id) {
      uni.showToast({ title: '内容不存在', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 800)
      return
    }
    this.loadContent()
  },
  methods: {
    async loadContent() {
      try {
        const res = await fetchContentDetail(this.id)
        const data = res.data || res
        const detail = data.data || data
        this.form.contentText = detail.contentText || ''
        if (detail.tags) {
          const tags = typeof detail.tags === 'string' ? detail.tags.split(',') : detail.tags
          this.tagsInput = tags.map((t) => t.trim()).filter(Boolean).join(',')
        }
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    async handleSave() {
      this.form.tags = this.parsedTags.join(',')
      this.saving = true
      try {
        await updateContent(this.id, this.form)
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 800)
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '保存失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.scroll {
  padding: 24rpx 0;
}

.section {
  background-color: #ffffff;
  margin-bottom: 24rpx;
}

.cell {
  padding: 24rpx;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  border-bottom-width: 1px;
  border-bottom-color: #f3f4f6;
  border-bottom-style: solid;
}

.cell:last-child {
  border-bottom: none;
}

.cell-label {
  width: 160rpx;
  font-size: 30rpx;
  color: #111827;
  padding-top: 4rpx;
}

.cell-input {
  flex: 1;
  font-size: 30rpx;
  color: #111827;
}

.cell-textarea {
  flex: 1;
  min-height: 120rpx;
  font-size: 30rpx;
  color: #111827;
  line-height: 1.6;
}

.tags-preview {
  padding: 0 24rpx 24rpx;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag-chip {
  font-size: 22rpx;
  color: #3b82f6;
  background-color: #eff6ff;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.actions {
  padding: 0 24rpx 40rpx;
}

.btn-save {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  background-color: #111827;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
}
</style>

