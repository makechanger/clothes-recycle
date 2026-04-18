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

    <!-- 回收员端首页：待接单列表 -->
    <view v-else-if="userStore.isCollector" class="home-content">
      <view class="welcome">
        <text class="welcome-text">回收员工作台</text>
        <text class="welcome-sub">以下是当前待接单的订单</text>
      </view>

      <!-- 待接单列表 -->
      <view v-if="pendingOrders.length > 0" class="pending-list">
        <view
          v-for="order in pendingOrders"
          :key="order.id"
          class="pending-card"
          @click="goOrderDetail(order.id)"
        >
          <!-- 订单头部：订单号 + 状态 -->
          <view class="pending-header">
            <text class="pending-no">{{ order.orderNo }}</text>
            <text class="pending-status">待接单</text>
          </view>
          <!-- 地址信息 -->
          <view class="pending-row">
            <text class="pending-label">上门地址</text>
            <text class="pending-value">{{ parseAddress(order.addressSnapshot) }}</text>
          </view>
          <!-- 预约时间 -->
          <view class="pending-row">
            <text class="pending-label">预约时间</text>
            <text class="pending-value">{{ order.appointmentDate }} {{ order.timeSlotStart }}-{{ order.timeSlotEnd }}</text>
          </view>
          <!-- 衣物分类 -->
          <view class="pending-row">
            <text class="pending-label">衣物分类</text>
            <text class="pending-value">{{ parseCategories(order.clothesCategories) }}</text>
          </view>
          <!-- 预估重量 -->
          <view class="pending-row">
            <text class="pending-label">预估重量</text>
            <text class="pending-value">{{ order.estimatedWeight }}kg</text>
          </view>
          <!-- 接单按钮 -->
          <view class="pending-actions" @click.stop="">
            <button class="accept-btn" size="mini" @click.stop="handleAccept(order)">接单</button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty">
        <text class="empty-text">暂无待接单订单</text>
      </view>
    </view>

    <!-- 机构端首页：扫码接收 + 最近接收订单 -->
    <view v-else-if="userStore.isInstitution" class="home-content">
      <view class="welcome">
        <text class="welcome-text">机构工作台</text>
        <text class="welcome-sub">扫描二维码接收回收订单</text>
      </view>

      <!-- 扫码接收按钮 -->
      <view class="scan-area">
        <button class="scan-btn" @click="handleScan">
          <text class="scan-icon">📷</text>
          <text class="scan-text">扫码接收</text>
        </button>
      </view>

      <!-- 最近接收的订单 -->
      <view class="recent-section">
        <view class="recent-header">
          <text class="recent-title">最近接收</text>
          <text class="recent-more" @click="goTo('/pages/order/list/list')">查看全部 ></text>
        </view>

        <view v-if="receivedOrders.length > 0">
          <view
            v-for="order in receivedOrders"
            :key="order.id"
            class="received-card"
          >
            <!-- 订单头部：订单号 + 状态 -->
            <view class="pending-header">
              <text class="pending-no">{{ order.orderNo }}</text>
              <text class="received-status">已接收</text>
            </view>
            <!-- 实际重量 -->
            <view v-if="order.actualWeight" class="pending-row">
              <text class="pending-label">实际重量</text>
              <text class="pending-value">{{ order.actualWeight }}kg</text>
            </view>
            <!-- 衣物分类 -->
            <view class="pending-row">
              <text class="pending-label">衣物分类</text>
              <text class="pending-value">{{ parseCategories(order.clothesCategories) }}</text>
            </view>
            <!-- 发放积分 -->
            <view v-if="order.pointsAwarded" class="pending-row">
              <text class="pending-label">发放积分</text>
              <text class="pending-value points-value">+{{ order.pointsAwarded }}</text>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty">
          <text class="empty-text">暂无接收记录</text>
        </view>
      </view>
    </view>

    <!-- 自定义底部导航 -->
    <custom-tabbar :current="0" />
  </view>
</template>

<script setup>
/**
 * 首页
 * - 未登录：欢迎页 + 登录入口
 * - 用户：快捷入口（预约回收、我的订单、地址管理、个人中心）
 * - 回收员：待接单列表 + 接单操作
 * - 机构：占位（后续开发）
 */
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import request from '@/utils/request.js'

const userStore = useUserStore()

// ==================== 回收员待接单数据 ====================

// 待接单订单列表
const pendingOrders = ref([])

// ==================== 机构端数据 ====================

// 机构最近接收的订单列表
const receivedOrders = ref([])

// 页面每次显示时刷新数据（根据角色加载不同数据）
onShow(() => {
  if (userStore.isCollector) {
    loadPendingOrders()
  }
  if (userStore.isInstitution) {
    loadReceivedOrders()
  }
})

/**
 * 加载待接单订单列表
 */
async function loadPendingOrders() {
  try {
    const data = await request({ url: '/api/collector/order/pending', loading: false })
    pendingOrders.value = data || []
  } catch (e) {
    console.error('加载待接单列表失败:', e)
  }
}

/**
 * 加载机构最近接收的订单（取前5条）
 */
async function loadReceivedOrders() {
  try {
    const data = await request({ url: '/api/institution/order/list', loading: false })
    // 只展示最近5条
    receivedOrders.value = (data || []).slice(0, 5)
  } catch (e) {
    console.error('加载机构接收订单失败:', e)
  }
}

/**
 * 机构扫码接收订单
 * 调用 uni.scanCode 扫描回收员生成的溯源二维码，提取订单编号后调用后端接收接口
 */
function handleScan() {
  uni.scanCode({
    scanType: ['qrCode'],
    success: async (res) => {
      const orderNo = res.result
      if (!orderNo) {
        uni.showToast({ title: '二维码内容为空', icon: 'none' })
        return
      }
      try {
        await request({
          url: '/api/institution/order/receive',
          method: 'POST',
          data: { orderNo }
        })
        uni.showToast({ title: '接收成功', icon: 'success' })
        // 刷新最近接收列表
        loadReceivedOrders()
      } catch (e) {
        console.error('扫码接收失败:', e)
      }
    },
    fail: (err) => {
      // 用户取消扫码不提示错误
      if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
        uni.showToast({ title: '扫码失败', icon: 'none' })
      }
    }
  })
}

// ==================== 工具方法 ====================

/**
 * 解析地址快照 JSON，返回简短地址文本
 */
function parseAddress(snapshot) {
  if (!snapshot) return '未知地址'
  try {
    const addr = JSON.parse(snapshot)
    return `${addr.district || ''} ${addr.detailAddress || ''}`
  } catch {
    return '未知地址'
  }
}

/**
 * 解析衣物分类 JSON
 */
function parseCategories(json) {
  if (!json) return '未填写'
  try {
    return JSON.parse(json).join('、')
  } catch {
    return json
  }
}

// ==================== 操作方法 ====================

// 页面跳转
function goTo(url) {
  uni.navigateTo({ url })
}

// 跳转订单详情
function goOrderDetail(id) {
  uni.navigateTo({ url: `/pages/order/detail/detail?id=${id}` })
}

/**
 * 接单操作（二次确认）
 */
function handleAccept(order) {
  uni.showModal({
    title: '确认接单',
    content: `确定接受该订单吗？\n预约时间：${order.appointmentDate} ${order.timeSlotStart}-${order.timeSlotEnd}`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/collector/order/${order.id}/accept`,
            method: 'POST'
          })
          uni.showToast({ title: '接单成功', icon: 'success' })
          // 刷新待接单列表
          loadPendingOrders()
        } catch (e) {
          console.error('接单失败:', e)
        }
      }
    }
  })
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

/* ==================== 回收员待接单列表样式 ==================== */

/* 待接单卡片 */
.pending-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

/* 卡片头部 */
.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.pending-no {
  font-size: 26rpx;
  color: #999;
}

.pending-status {
  font-size: 24rpx;
  color: #ff9800;
  background: #fff3e0;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
}

/* 信息行 */
.pending-row {
  display: flex;
  margin-bottom: 10rpx;
  font-size: 26rpx;
  line-height: 1.5;
}

.pending-label {
  width: 140rpx;
  color: #999;
  flex-shrink: 0;
}

.pending-value {
  flex: 1;
  color: #333;
}

/* 接单按钮区域 */
.pending-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.accept-btn {
  background: #07c160 !important;
  color: #fff !important;
  border: none !important;
  font-size: 26rpx;
  padding: 0 40rpx;
  height: 60rpx;
  line-height: 60rpx;
  border-radius: 30rpx;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* ==================== 机构端样式 ==================== */

/* 扫码按钮区域 */
.scan-area {
  display: flex;
  justify-content: center;
  margin: 20rpx 0 40rpx;
}

.scan-btn {
  width: 400rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #07c160;
  color: #fff;
  border-radius: 50rpx;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.3);
}

.scan-icon {
  font-size: 40rpx;
  margin-right: 12rpx;
}

.scan-text {
  font-size: 32rpx;
  font-weight: bold;
}

/* 最近接收区域 */
.recent-section {
  margin-top: 20rpx;
}

.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.recent-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.recent-more {
  font-size: 24rpx;
  color: #07c160;
}

/* 已接收订单卡片 */
.received-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.received-status {
  font-size: 24rpx;
  color: #1565c0;
  background: #e3f2fd;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
}

/* 积分高亮 */
.points-value {
  color: #f44336;
  font-weight: bold;
}
</style>
