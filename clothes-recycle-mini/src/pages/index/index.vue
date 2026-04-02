<template>
  <!-- 首页：未登录显示欢迎页，登录后根据角色展示不同内容 -->
  <view class="page">
    <!-- 未登录状态：显示欢迎信息和基本入口 -->
    <view v-if="!userStore.isLoggedIn" class="home-content">
      <view class="welcome">
        <text class="welcome-text">欢迎使用衣物回收平台</text>
        <text class="welcome-sub">让旧衣物焕发新价值</text>
      </view>

      <!-- 未登录时的简单提示 -->
      <view class="guest-tips">
        <text class="tip-text">登录后可预约衣物回收、查看订单等</text>
        <button class="login-tip-btn" @click="goTo('/pages/login/login')">
          立即登录
        </button>
      </view>
    </view>

    <!-- 用户端首页 -->
    <view v-else-if="userStore.isUser" class="home-content">
      <view class="welcome">
        <text class="welcome-text">欢迎使用衣物回收平台</text>
        <text class="welcome-sub">让旧衣物焕发新价值</text>
      </view>

      <!-- 快捷入口 -->
      <view class="quick-actions">
        <view class="action-item" @click="goTo('/pages/order/create/create')">
          <text class="action-icon">♻️</text>
          <text class="action-text">预约回收</text>
        </view>
        <view class="action-item" @click="goTo('/pages/order/list/list')">
          <text class="action-icon">📋</text>
          <text class="action-text">我的订单</text>
        </view>
        <view class="action-item" @click="goTo('/pages/address/list/list')">
          <text class="action-icon">📍</text>
          <text class="action-text">地址管理</text>
        </view>
        <view class="action-item" @click="goTo('/pages/user/user')">
          <text class="action-icon">👤</text>
          <text class="action-text">个人中心</text>
        </view>
      </view>
    </view>

    <!-- 回收员端首页：待接单列表（后续开发） -->
    <view v-else-if="userStore.isCollector" class="home-content">
      <view class="welcome">
        <text class="welcome-text">回收员工作台</text>
        <text class="welcome-sub">待接单列表将在后续步骤开发</text>
      </view>
    </view>

    <!-- 机构端首页：接收任务（后续开发） -->
    <view v-else-if="userStore.isInstitution" class="home-content">
      <view class="welcome">
        <text class="welcome-text">机构工作台</text>
        <text class="welcome-sub">接收任务将在后续步骤开发</text>
      </view>
    </view>

    <!-- 自定义底部导航 -->
    <custom-tabbar :current="0" />
  </view>
</template>

<script setup>
import { useUserStore } from '@/store/user.js'

const userStore = useUserStore()

// 页面跳转
function goTo(url) {
  uni.navigateTo({ url })
}
</script>

<style scoped>
.page {
  padding-bottom: 120rpx;
}

.home-content {
  padding: 30rpx;
}

.welcome {
  text-align: center;
  padding: 60rpx 0;
}

.welcome-text {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #2e7d32;
  margin-bottom: 16rpx;
}

.welcome-sub {
  font-size: 26rpx;
  color: #999;
}

/* 未登录提示区域 */
.guest-tips {
  text-align: center;
  padding: 40rpx 0;
}

.tip-text {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 30rpx;
}

.login-tip-btn {
  width: 60%;
  height: 80rpx;
  line-height: 80rpx;
  background: #07c160;
  color: #fff;
  border-radius: 40rpx;
  font-size: 30rpx;
  border: none;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
  margin-top: 40rpx;
}

.action-item {
  width: calc(50% - 12rpx);
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.action-icon {
  font-size: 56rpx;
  margin-bottom: 16rpx;
}

.action-text {
  font-size: 28rpx;
  color: #333;
}
</style>
