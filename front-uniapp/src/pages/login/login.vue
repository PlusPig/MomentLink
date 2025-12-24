<template>
  <view class="page">
    <view class="header">
      <image class="avatar" src="/static/logo.png" mode="aspectFill" />
      <view class="headline">MomentLink|不止瞬间</view>
      <view class="sub">欢迎回来，登录后继续</view>
    </view>

    <view class="card">
      <view class="field">
        <text class="label">用户名</text>
        <input class="input" v-model.trim="form.username" placeholder="请输入用户名" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <text class="label">密码</text>
        <input class="input" v-model="form.password" type="password" password placeholder="请输入密码" placeholder-class="placeholder" />
      </view>

      <button class="btn" :loading="loading" :disabled="loading" @click="onSubmit">登录</button>

      <view class="row">
        <view class="hint">还没有账号？</view>
        <text class="link" @click="goRegister">去注册</text>
      </view>
    </view>

    <view class="footer">登录即表示你同意相关服务条款</view>
  </view>
</template>

<script>
import { login } from '@/api/auth'

export default {
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: '',
      },
    }
  },
  onShow() {
    const authToken = uni.getStorageSync('authToken')
    if (authToken) {
      uni.reLaunch({ url: '/pages/index/index' })
    }
  },
  methods: {
    goRegister() {
      uni.navigateTo({ url: '/pages/register/register' })
    },
    async onSubmit() {
      if (!this.form.username) {
        uni.showToast({ title: '请输入用户名', icon: 'none' })
        return
      }
      if (!this.form.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      this.loading = true
      try {
        const res = await login({
          username: this.form.username,
          password: this.form.password,
        })

        const { token, type, user } = (res && res.data) || {}
        if (!token) {
          uni.showToast({ title: '登录失败：缺少token', icon: 'none' })
          return
        }

        const authToken = `${type || 'Bearer'} ${token}`
        uni.setStorageSync('authToken', authToken)
        uni.setStorageSync('user', user || null)

        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 300)
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '登录失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 80rpx 48rpx 40rpx;
  background: linear-gradient(180deg, #0ea5e9 0%, #f8fafc 55%, #f8fafc 100%);
}

.header {
  padding: 20rpx 0 40rpx;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 64rpx;
  background: rgba(255, 255, 255, 0.6);
}

.headline {
  margin-top: 24rpx;
  font-size: 56rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1rpx;
}

.sub {
  margin-top: 12rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.card {
  margin-top: 18rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28rpx;
  padding: 36rpx 28rpx;
  box-shadow: 0 18rpx 60rpx rgba(15, 23, 42, 0.12);
}

.field {
  margin-bottom: 22rpx;
}

.label {
  display: block;
  font-size: 24rpx;
  color: #334155;
  margin-bottom: 10rpx;
}

.input {
  height: 92rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: #f1f5f9;
  color: #0f172a;
  font-size: 30rpx;
}

.placeholder {
  color: #94a3b8;
}

.btn {
  margin-top: 10rpx;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 18rpx;
  background: #0ea5e9;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
}

.row {
  margin-top: 26rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.hint {
  font-size: 26rpx;
  color: #64748b;
}

.link {
  margin-left: 10rpx;
  font-size: 26rpx;
  color: #0ea5e9;
}

.footer {
  margin-top: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: rgba(15, 23, 42, 0.5);
}
</style>
