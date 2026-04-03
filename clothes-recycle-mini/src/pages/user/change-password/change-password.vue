<template>
  <!-- 修改密码页面：输入旧密码和新密码，调用后端修改密码接口 -->
  <view class="page">
    <view class="form">
      <!-- 旧密码输入 -->
      <view class="form-item">
        <text class="label">旧密码</text>
        <input
          v-model="oldPassword"
          class="input-field"
          type="password"
          placeholder="请输入当前密码"
        />
      </view>

      <!-- 新密码输入 -->
      <view class="form-item">
        <text class="label">新密码</text>
        <input
          v-model="newPassword"
          class="input-field"
          type="password"
          placeholder="请输入新密码（至少6位）"
        />
      </view>

      <!-- 确认新密码输入 -->
      <view class="form-item">
        <text class="label">确认新密码</text>
        <input
          v-model="confirmPassword"
          class="input-field"
          type="password"
          placeholder="请再次输入新密码"
        />
      </view>

      <!-- 提交按钮 -->
      <button class="submit-btn" @click="handleSubmit">确认修改</button>
    </view>
  </view>
</template>

<script setup>
/**
 * 修改密码页面
 * - 所有角色（用户/回收员/机构）共用此页面
 * - 重构后统一调用 /api/user/changePassword 接口
 * - 修改成功后自动退出登录，要求重新登录
 */
import { ref } from 'vue'
import { useUserStore } from '@/store/user.js'
import request from '@/utils/request.js'

const userStore = useUserStore()

// 表单数据
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

/** 提交修改密码 */
async function handleSubmit() {
  // 1. 前端校验
  if (!oldPassword.value) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少需要6位', icon: 'none' })
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
    return
  }
  if (oldPassword.value === newPassword.value) {
    uni.showToast({ title: '新密码不能与旧密码相同', icon: 'none' })
    return
  }

  try {
    // 2. 统一调用修改密码接口（所有角色都用同一个接口）
    await request({
      url: '/api/user/changePassword',
      method: 'POST',
      data: {
        oldPassword: oldPassword.value,
        newPassword: newPassword.value
      }
    })

    // 3. 修改成功，提示后退出登录重新登录
    uni.showModal({
      title: '修改成功',
      content: '密码已修改，请重新登录',
      showCancel: false,
      success: () => {
        userStore.logout()
      }
    })
  } catch (e) {
    console.error('修改密码失败:', e)
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
}

.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

/* 表单项样式 */
.form-item {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.input-field {
  width: 100%;
  height: 88rpx;
  background: #f8f8f8;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  text-align: center;
  border: none;
  margin-top: 40rpx;
}
</style>
