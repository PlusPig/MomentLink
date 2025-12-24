<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="section">
        <view class="cell">
          <text class="cell-label">旧密码</text>
          <input
            class="cell-input"
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            placeholder-style="color:#9ca3af"
          />
        </view>
        <view class="cell">
          <text class="cell-label">新密码</text>
          <input
            class="cell-input"
            v-model="form.newPassword"
            type="password"
            placeholder="6-20位，包含字母和数字"
            placeholder-style="color:#9ca3af"
          />
        </view>
        <view class="cell">
          <text class="cell-label">确认密码</text>
          <input
            class="cell-input"
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            placeholder-style="color:#9ca3af"
          />
        </view>
      </view>

      <view class="actions">
        <button class="btn-save" :loading="saving" @click="handleSave">保存</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { updatePassword } from '@/api/user'

export default {
  data() {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      },
      saving: false,
    }
  },
  methods: {
    async handleSave() {
      if (!this.form.oldPassword) {
        uni.showToast({ title: '请输入旧密码', icon: 'none' })
        return
      }
      if (!this.form.newPassword) {
        uni.showToast({ title: '请输入新密码', icon: 'none' })
        return
      }
      if (this.form.newPassword !== this.form.confirmPassword) {
        uni.showToast({ title: '两次密码不一致', icon: 'none' })
        return
      }
      if (!/^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/.test(this.form.newPassword)) {
        uni.showToast({ title: '密码必须是6-20位，且包含字母和数字', icon: 'none' })
        return
      }

      this.saving = true
      try {
        await updatePassword(this.form)
        uni.showToast({ title: '密码修改成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 800)
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '修改失败', icon: 'none' })
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

.actions {
  padding: 24rpx;
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

