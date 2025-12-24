<template>
  <view v-if="show" class="image-preview-modal" @click="handleClose">
    <view class="preview-backdrop" @click.stop></view>
    <view class="preview-content" @click.stop>
      <swiper
        class="preview-swiper"
        :current="currentIndex"
        @change="onSwiperChange"
        :indicator-dots="images.length > 1"
        indicator-color="rgba(255, 255, 255, 0.3)"
        indicator-active-color="rgba(255, 255, 255, 0.9)"
      >
        <swiper-item v-for="(url, idx) in images" :key="idx" class="preview-item">
          <image class="preview-image" :src="resolveUrl(url)" mode="aspectFit" />
        </swiper-item>
      </swiper>
      <view class="preview-close" @click="handleClose">
        <iconify-icon icon="mdi:close" class="close-icon" />
      </view>
    </view>
  </view>
</template>

<script>
import { getBaseUrl } from '@/utils/request'

export default {
  name: 'ImagePreview',
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    images: {
      type: Array,
      default: () => [],
    },
    current: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      currentIndex: 0,
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.currentIndex = this.current
      }
    },
    current(newVal) {
      this.currentIndex = newVal
    },
  },
  methods: {
    resolveUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      if (url.startsWith('/api/')) return url
      if (url.startsWith('/uploads/')) {
        return `${getBaseUrl()}${url}`
      }
      return `${getBaseUrl()}${url.startsWith('/') ? url : '/' + url}`
    },
    onSwiperChange(e) {
      this.currentIndex = e.detail.current
      this.$emit('change', e.detail.current)
    },
    handleClose() {
      this.$emit('close')
    },
  },
}
</script>

<style scoped>
.image-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.3s ease-out;
}

.preview-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
}

.preview-content {
  position: relative;
  width: 100%;
  height: 80vh;
  max-height: 80vh;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(40rpx) saturate(180%);
  -webkit-backdrop-filter: blur(40rpx) saturate(180%);
  border-top-left-radius: 32rpx;
  border-top-right-radius: 32rpx;
  overflow: hidden;
  animation: slideUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-sizing: border-box;
}

.preview-swiper {
  width: 100%;
  height: 100%;
}

.preview-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-close {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.preview-close .close-icon {
  font-size: 32rpx;
  color: #ffffff;
}

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

