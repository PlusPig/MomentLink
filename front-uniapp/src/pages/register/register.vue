<template>
  <view class="page">
    <view class="header">
      <view class="headline">创建账号</view>
      <view class="sub">加入 MomentLink，开启你的社交之旅</view>
    </view>

    <view class="card">
      <view class="field">
        <text class="label">用户名</text>
        <input class="input" v-model.trim="form.username" placeholder="3-50个字符" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <text class="label">昵称（可选）</text>
        <input class="input" v-model.trim="form.nickname" placeholder="不填默认同用户名" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <text class="label">邮箱（可选）</text>
        <input class="input" v-model.trim="form.email" type="text" placeholder="name@example.com" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <text class="label">密码</text>
        <input class="input" v-model="form.password" type="password" password placeholder="6-20位" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <text class="label">确认密码</text>
        <input class="input" v-model="form.confirmPassword" type="password" password placeholder="请再次输入密码" placeholder-class="placeholder" />
      </view>

      <button class="btn" :loading="loading" :disabled="loading" @click="onSubmit">注册并登录</button>

      <view class="row">
        <view class="hint">已有账号？</view>
        <text class="link" @click="goLogin">去登录</text>
      </view>
    </view>

    <view class="footer">注册即表示你同意相关服务条款</view>
  </view>
</template>

<script>
import { register } from '@/api/auth'

export default {
  data() {
    return {
      loading: false,
      form: {
        username: '',
        nickname: '',
        email: '',
        password: '',
        confirmPassword: '',
      },
    }
  },
  methods: {
    goLogin() {
      uni.navigateBack({
        delta: 1,
        fail: () => {
          uni.redirectTo({ url: '/pages/login/login' })
        },
      })
    },
    async onSubmit() {
      if (!this.form.username || this.form.username.length < 3) {
        uni.showToast({ title: '用户名至少3个字符', icon: 'none' })
        return
      }
      if (!this.form.password || this.form.password.length < 6) {
        uni.showToast({ title: '密码至少6位', icon: 'none' })
        return
      }
      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({ title: '两次密码不一致', icon: 'none' })
        return
      }

      this.loading = true
      try {
        const payload = {
          username: this.form.username,
          password: this.form.password,
        }
        if (this.form.email) payload.email = this.form.email
        if (this.form.nickname) payload.nickname = this.form.nickname

        const res = await register(payload)

        const { token, type, user } = (res && res.data) || {}
        if (!token) {
          uni.showToast({ title: '注册失败：缺少token', icon: 'none' })
          return
        }

        const authToken = `${type || 'Bearer'} ${token}`
        uni.setStorageSync('authToken', authToken)
        uni.setStorageSync('user', user || null)

        uni.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 300)
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '注册失败', icon: 'none' })
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
  background: linear-gradient(180deg, #22c55e 0%, #f8fafc 55%, #f8fafc 100%);
}

.header {
  padding: 20rpx 0 40rpx;
}

.headline {
  font-size: 56rpx;
  font-weight: 700;
  color: #ffffff;
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
  background: #22c55e;
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
  color: #16a34a;
}

.footer {
  margin-top: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: rgba(15, 23, 42, 0.5);
}
</style>
