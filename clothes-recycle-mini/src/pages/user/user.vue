<template>
  <!-- 个人中心页：未登录时自动跳转登录页 -->
  <view class="page">
    <view class="user-header">
      <text class="user-name">{{ userStore.userInfo?.name || '未设置姓名' }}</text>
      <text class="user-phone">{{ userStore.userInfo?.phone || '未绑定手机号' }}</text>
      <text class="user-role">当前角色：{{ roleText }}</text>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-list">
      <view v-if="userStore.isUser" class="menu-item" @click="goTo('/pages/address/list/list')">
        <text>地址管理</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item" @click="goTo('/pages/user/change-password/change-password')">
        <text>修改密码</text>
        <text class="arrow">></text>
      </view>
      <view v-if="userStore.isUser" class="menu-item" @click="goTo('/pages/user/apply-role/apply-role')">
        <text>资质申请</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item logout" @click="handleLogout">
        <text>退出登录</text>
      </view>
    </view>

    <!-- 自定义底部导航 -->
    <custom-tabbar :current="userStore.isUser ? 3 : (userStore.isCollector ? 2 : 1)" />
  </view>
</template>

<script setup>
/**
 * 个人中心页
 * - 未登录时自动跳转到登录页
 * - 登录后显示用户信息和功能菜单
 */
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'

const userStore = useUserStore()

// 页面显示时检查登录状态，未登录则跳转登录页
onShow(() => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
  }
})

// 角色中文显示
const roleText = computed(() => {
  const map = { USER: '普通用户', COLLECTOR: '回收员', INSTITUTION: '机构' }
  return map[userStore.role] || '未知'
})

function goTo(url) {
  uni.navigateTo({ url })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}
</script>

<style scoped>
.page { padding-bottom: 120rpx; }

.user-header {
  background: linear-gradient(135deg, #07c160, #2e7d32);
  padding: 60rpx 30rpx 40rpx;
  color: #fff;
}

.user-name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.user-phone {
  display: block;
  font-size: 26rpx;
  opacity: 0.8;
  margin-bottom: 8rpx;
}

.user-role {
  display: block;
  font-size: 24rpx;
  opacity: 0.7;
}

.menu-list {
  margin-top: 20rpx;
  background: #fff;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  font-size: 28rpx;
}

.arrow { color: #ccc; }

.logout {
  color: #e53935;
  justify-content: center;
  margin-top: 40rpx;
}
</style>
