<template>
  <view class="login-page">
    
    <view class="back-btn" @tap="goBack">
      <text class="back-icon">⬅</text>
      <text class="back-text">返回</text>
    </view>
      <!-- 登录页面：统一手机号+密码登录，后端自动识别角色 -->
    <view class="login-header">
      <text class="title">衣物回收平台</text>
      <text class="subtitle">让旧衣物焕发新价值</text>
    </view>
    <!-- 统一登录表单 -->
    <view class="login-form">
      <input v-model="phone" class="input-field" type="text" placeholder="请输入手机号" maxlength="11" />
      <input v-model="password" class="input-field" type="password" placeholder="请输入密码" />
      <button class="login-btn" @click="handleLogin">
        登 录
      </button>
    </view>
  </view>
</template>

<script setup>
/**
 * 登录页
 * - 所有角色统一使用手机号+密码登录
 * - 后端自动识别角色（用户/回收员/机构），返回 role 字段
 * - 登录成功后根据角色跳转到对应首页
 */
import { ref } from 'vue'
import { useUserStore } from '@/store/user.js'
import request from '@/utils/request.js'

const userStore = useUserStore()

// 登录表单
const phone = ref('')
const password = ref('')

// 回退上一页
function goBack() {
  uni.navigateBack({
    delta: 1,
    fail: () => {
      // 兜底：如果无法回退，不再使用 switchTab，改用 reLaunch
      uni.reLaunch({
        url: '/pages/index/index' // 这里保持你的首页路径不变
      })
    }
  })
}

// 统一登录
async function handleLogin() {
  if (!phone.value || !password.value) {
    uni.showToast({ title: '请输入手机号和密码', icon: 'none' })
    return
  }

  try {
    // 调用统一登录接口，后端根据手机号自动识别角色
    const data = await request({
      url: '/api/auth/login',
      method: 'POST',
      data: { phone: phone.value, password: password.value }
    })

    // 保存登录状态，角色由后端返回（USER / COLLECTOR / INSTITUTION）
    userStore.login({
      token: data.token,
      userInfo: data.userInfo,
      role: data.role
    })

    // 登录成功，跳转到首页
    uni.reLaunch({ url: '/pages/index/index' })
  } catch (e) {
    console.error('登录失败:', e)
  }
}
</script>

<style scoped>
/* 回退按钮样式 */
.back-btn {
  position: absolute;
  top: 80rpx;
  left: 40rpx;
  display: flex;
  align-items: center;
  z-index: 10;
  padding: 10rpx 20rpx;
}

.back-icon {
  font-size: 36rpx;
  color: #333;
  margin-right: 8rpx;
}

.back-text {
  font-size: 28rpx;
  color: #333;
}

.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 180rpx 60rpx 0; /* 微微调大了 padding-top 防止遮挡按钮 */
  background: linear-gradient(180deg, #e8f5e9 0%, #ffffff 50%);
  position: relative; /* 配合返回按钮的绝对定位 */
}

.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #2e7d32;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 28rpx;
  color: #666;
}

.login-form {
  width: 100%;
}

.input-field {
  width: 100%;
  height: 88rpx;
  background: #fff;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  text-align: center;
  border: none;
  margin-top: 20rpx;
}
</style>