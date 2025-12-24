<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="section">
        <view class="cell">
          <text class="cell-label">昵称</text>
          <input
            class="cell-input"
            v-model="form.nickname"
            placeholder="请输入昵称"
            placeholder-style="color:#9ca3af"
          />
        </view>
        <view class="cell">
          <text class="cell-label">个性签名</text>
          <textarea
            class="cell-textarea"
            v-model="form.signature"
            placeholder="写点什么吧..."
            placeholder-style="color:#9ca3af"
            :maxlength="200"
            auto-height
          />
        </view>
        <view class="cell" @click="showGenderPicker">
          <text class="cell-label">性别</text>
          <text class="cell-value">{{ genderText }}</text>
          <iconify-icon icon="mdi:chevron-right" class="chevron" />
        </view>
      </view>

      <view class="actions">
        <button class="btn-save" :loading="saving" @click="handleSave">保存</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getCurrentUser, updateProfile } from '@/api/user'

export default {
  data() {
    return {
      form: {
        nickname: '',
        signature: '',
        gender: 0,
      },
      saving: false,
      genderOptions: [
        { label: '未知', value: 0 },
        { label: '男', value: 1 },
        { label: '女', value: 2 },
      ],
    }
  },
  computed: {
    genderText() {
      const option = this.genderOptions.find((o) => o.value === this.form.gender)
      return option ? option.label : '未知'
    },
  },
  onLoad() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await getCurrentUser()
        const data = res.data || res
        const user = data.data || data || {}
        this.form.nickname = user.nickname || ''
        this.form.signature = user.signature || ''
        this.form.gender = user.gender !== undefined ? user.gender : 0
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    showGenderPicker() {
      const items = this.genderOptions.map((o) => o.label)
      const currentIndex = this.genderOptions.findIndex((o) => o.value === this.form.gender)
      uni.showActionSheet({
        itemList: items,
        success: (res) => {
          this.form.gender = this.genderOptions[res.tapIndex].value
        },
      })
    },
    async handleSave() {
      this.saving = true
      try {
        await updateProfile(this.form)
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
  align-items: center;
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
}

.cell-input {
  flex: 1;
  font-size: 30rpx;
  color: #111827;
  text-align: right;
}

.cell-textarea {
  flex: 1;
  min-height: 80rpx;
  font-size: 30rpx;
  color: #111827;
  text-align: right;
}

.cell-value {
  flex: 1;
  font-size: 30rpx;
  color: #111827;
  text-align: right;
}

.chevron {
  font-size: 28rpx;
  color: #d1d5db;
  margin-left: 8rpx;
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

