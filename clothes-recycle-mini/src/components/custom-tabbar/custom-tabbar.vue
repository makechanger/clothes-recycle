<template>
  <!-- 自定义底部导航栏：根据用户角色显示不同的 tab 项 -->
  <view class="tabbar">
    <view
      v-for="(item, index) in currentTabs"
      :key="index"
      class="tabbar-item"
      :class="{ active: current === index }"
      @click="switchTab(index)"
    >
      <text class="tabbar-icon">{{ item.icon }}</text>
      <text class="tabbar-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
/**
 * 自定义 TabBar 组件
 * 根据 Pinia 中存储的 role 动态渲染不同角色的底部导航
 * - 未登录：首页、个人中心（点个人中心会跳登录页）
 * - USER：首页、预约回收、我的订单、个人中心
 * - COLLECTOR：待接单、我的任务、个人中心
 * - INSTITUTION：接收任务、个人中心
 */
import { computed } from 'vue'
import { useUserStore } from '@/store/user.js'

const props = defineProps({
  // 当前激活的 tab 索引
  current: {
    type: Number,
    default: 0
  }
})

const userStore = useUserStore()

// 未登录时的 TabBar 配置（仅首页和个人中心）
const guestTabs = [
  { text: '首页', icon: '🏠', path: '/pages/index/index' },
  { text: '个人中心', icon: '👤', path: '/pages/login/login' }
]

// 用户端 TabBar 配置
const userTabs = [
  { text: '首页', icon: '🏠', path: '/pages/index/index' },
  { text: '预约回收', icon: '♻️', path: '/pages/order/create/create' },
  { text: '我的订单', icon: '📋', path: '/pages/order/list/list' },
  { text: '个人中心', icon: '👤', path: '/pages/user/user' }
]

// 回收员端 TabBar 配置
const collectorTabs = [
  { text: '待接单', icon: '📦', path: '/pages/index/index' },
  { text: '我的任务', icon: '📋', path: '/pages/order/list/list' },
  { text: '个人中心', icon: '👤', path: '/pages/user/user' }
]

// 机构端 TabBar 配置
const institutionTabs = [
  { text: '接收任务', icon: '📥', path: '/pages/index/index' },
  { text: '接收记录', icon: '📋', path: '/pages/order/list/list' },
  { text: '衣物去向', icon: '📦', path: '/pages/destination/list/list' },
  { text: '个人中心', icon: '👤', path: '/pages/user/user' }
]

// 根据当前登录状态和角色返回对应的 tab 配置
const currentTabs = computed(() => {
  if (!userStore.isLoggedIn) return guestTabs
  if (userStore.role === 'COLLECTOR') return collectorTabs
  if (userStore.role === 'INSTITUTION') return institutionTabs
  return userTabs
})

// 切换 tab 页面
function switchTab(index) {
  const tab = currentTabs.value[index]
  if (tab) {
    uni.reLaunch({ url: tab.path })
  }
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1rpx solid #e5e5e5;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 999;
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.tabbar-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
}

.tabbar-text {
  font-size: 20rpx;
  color: #999;
}

.tabbar-item.active .tabbar-text {
  color: #07c160;
  font-weight: bold;
}
</style>
