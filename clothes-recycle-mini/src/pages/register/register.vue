<template>
  <!-- 注册页面：手机号+密码，注册后自动登录，实名在下单时再要求 -->
  <view class="register-page">

    <!-- 返回按钮 -->
    <view class="back-btn" @tap="goBack">
      <text class="back-icon">⬅</text>
      <text class="back-text">返回</text>
    </view>

    <!-- 页面标题 -->
    <view class="register-header">
      <text class="title">注册账号</text>
      <text class="subtitle">注册后即可使用衣物回收服务</text>
    </view>

    <!-- 注册表单：仅手机号+密码，实名在下单时再要求 -->
    <view class="register-form">
      <!-- 手机号输入 -->
      <input
        v-model="phone"
        class="input-field"
        type="number"
        placeholder="请输入手机号"
        maxlength="11"
      />
      <!-- 密码输入 -->
      <input
        v-model="password"
        class="input-field"
        type="password"
        placeholder="请输入密码（至少6位）"
      />
      <!-- 确认密码 -->
      <input
        v-model="confirmPassword"
        class="input-field"
        type="password"
        placeholder="请再次输入密码"
      />
      <!-- 注册按钮 -->
      <button class="register-btn" @click="handleRegister">
        注 册
      </button>
    </view>

    <!-- 底部：已有账号去登录 -->
    <view class="footer-link" @tap="goLogin">
      <text>已有账号？</text>
      <text class="link-text">去登录</text>
    </view>
  </view>
</template>

<script setup>
/**
 * 注册页
 * - 所有角色共用一个注册入口，注册后均为普通用户（USER）
 * - 仅需手机号+密码，实名信息在操作订单时再要求填写
 * - 注册成功后自动登录并跳转首页
 */
import { ref } from 'vue'
import { useUserStore } from '@/store/user.js'
import request from '@/utils/request.js'

const userStore = useUserStore()

// 注册表单数据（姓名在下单时再要求实名）
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')

// 返回上一页
function goBack() {
  uni.navigateBack({
    delta: 1,
    fail: () => {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  })
}

// 跳转登录页
function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

// 提交注册
async function handleRegister() {
  // 前端校验
  if (!phone.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  if (!password.value || password.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }

  try {
    // 调用注册接口，注册成功后后端自动登录返回 token
    const data = await request({
      url: '/api/auth/register',
      method: 'POST',
      data: {
        phone: phone.value,
        password: password.value
      }
    })

    // 保存登录状态
    userStore.login({
      token: data.token,
      userInfo: data.userInfo,
      role: data.role
    })

    uni.showToast({ title: '注册成功', icon: 'success' })

    // 延迟跳转首页，让用户看到成功提示
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 1000)
  } catch (e) {
    console.error('注册失败:', e)
  }
}
</script>

<style scoped>
/* 返回按钮 */
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

/* 页面容器 */
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 180rpx 60rpx 0;
  background: linear-gradient(180deg, #e8f5e9 0%, #ffffff 50%);
  position: relative;
}

/* 标题区域 */
.register-header {
  text-align: center;
  margin-bottom: 60rpx;
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

/* 表单区域 */
.register-form {
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

.register-btn {
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

/* 底部链接 */
.footer-link {
  margin-top: 40rpx;
  font-size: 26rpx;
  color: #999;
}

.link-text {
  color: #07c160;
  margin-left: 8rpx;
}
</style>
