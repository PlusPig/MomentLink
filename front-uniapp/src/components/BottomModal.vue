<template>
  <view v-if="show" class="bottom-modal" @click="handleBackdropClick">
    <view class="modal-backdrop" @click.stop></view>
    <view class="modal-content" @click.stop :class="{ 'modal-show': show }">
      <!-- 标题栏 -->
      <view v-if="title || showClose" class="modal-header">
        <text v-if="title" class="modal-title">{{ title }}</text>
        <view v-if="showClose" class="modal-close" @click="handleClose">
          <iconify-icon icon="mdi:close" class="close-icon" />
        </view>
      </view>
      <!-- 内容插槽 -->
      <view class="modal-body">
        <slot></slot>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'BottomModal',
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: '',
    },
    showClose: {
      type: Boolean,
      default: true,
    },
    closeOnBackdrop: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    handleBackdropClick() {
      if (this.closeOnBackdrop) {
        this.handleClose()
      }
    },
  },
}
</script>

<style scoped>
.bottom-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.25s ease-out;
}

.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(8rpx);
  -webkit-backdrop-filter: blur(8rpx);
}

.modal-content {
  position: relative;
  width: 100%;
  background-color: #ffffff;
  border-top-left-radius: 24rpx;
  border-top-right-radius: 24rpx;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  max-height: 90vh;
  overflow: hidden;
  transform: translateY(100%);
  animation: slideUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.modal-content.modal-show {
  transform: translateY(0);
}

.modal-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx 20rpx;
  border-bottom: 0.5rpx solid #e5e5ea;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.modal-close {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background-color: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.modal-close:active {
  background-color: #e5e5ea;
}

.modal-close .close-icon {
  font-size: 28rpx;
  color: #000000;
}

.modal-body {
  padding: 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  max-height: calc(90vh - 120rpx);
  overflow-y: auto;
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

