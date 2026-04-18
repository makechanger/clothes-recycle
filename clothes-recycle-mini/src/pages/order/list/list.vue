<template>
  <!-- 订单列表页：用户和回收员共用，根据角色展示不同 Tab 和操作 -->
  <view class="page">
    <!-- 状态 Tab 筛选栏 -->
    <view class="tab-bar">
      <view
        v-for="tab in currentTabs"
        :key="tab.value"
        :class="['tab-item', { active: currentTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <view v-if="orderList.length > 0" class="order-list">
      <view
        v-for="order in orderList"
        :key="order.id"
        class="order-card"
        @click="goDetail(order.id)"
      >
        <!-- 订单头部：订单号 + 状态 -->
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text :class="['status-tag', 'status-' + order.status]">
            {{ statusMap[order.status] }}
          </text>
        </view>

        <!-- 订单信息 -->
        <view class="order-info">
          <!-- 地址信息（从地址快照解析） -->
          <view class="info-row">
            <text class="info-label">上门地址</text>
            <text class="info-value">{{ parseAddress(order.addressSnapshot) }}</text>
          </view>
          <!-- 预约时间 -->
          <view class="info-row">
            <text class="info-label">预约时间</text>
            <text class="info-value">{{ order.appointmentDate }} {{ order.timeSlotStart }}-{{ order.timeSlotEnd }}</text>
          </view>
          <!-- 衣物分类 -->
          <view class="info-row">
            <text class="info-label">衣物分类</text>
            <text class="info-value">{{ parseCategories(order.clothesCategories) }}</text>
          </view>
          <!-- 重量信息 -->
          <view class="info-row">
            <text class="info-label">预估重量</text>
            <text class="info-value">{{ order.estimatedWeight }}kg</text>
          </view>
          <!-- 实际重量（称重完成后显示） -->
          <view v-if="order.actualWeight" class="info-row">
            <text class="info-label">实际重量</text>
            <text class="info-value highlight">{{ order.actualWeight }}kg</text>
          </view>
        </view>

        <!-- 操作按钮区域 -->
        <view v-if="showActions(order)" class="order-actions" @click.stop="">
          <!-- ===== 用户操作 ===== -->
          <template v-if="isUser">
            <!-- 待接单/已接单 可取消 -->
            <button
              v-if="order.status === 0 || order.status === 1"
              class="action-btn cancel"
              size="mini"
              @click.stop="handleCancel(order)"
            >取消订单</button>
            <!-- 待确认 可确认完成 -->
            <button
              v-if="order.status === 3"
              class="action-btn confirm"
              size="mini"
              @click.stop="handleConfirm(order)"
            >确认完成</button>
          </template>

          <!-- ===== 回收员操作 ===== -->
          <template v-if="isCollector">
            <!-- 已接单 可开始上门 -->
            <button
              v-if="order.status === 1"
              class="action-btn pickup"
              size="mini"
              @click.stop="handlePickup(order)"
            >开始上门</button>
            <!-- 上门中 可完成称重 -->
            <button
              v-if="order.status === 2"
              class="action-btn confirm"
              size="mini"
              @click.stop="handleComplete(order)"
            >完成称重</button>
          </template>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty">
      <text class="empty-text">暂无订单</text>
    </view>

    <!-- 底部导航栏：机构"接收任务"索引0，回收员"我的任务"索引1，用户"我的订单"索引2 -->
    <custom-tabbar :current="isInstitution ? 0 : (isCollector ? 1 : 2)" />
  </view>
</template>

<script setup>
/**
 * 订单列表页（用户 + 回收员共用）
 * - 用户：Tab（全部/待接单/进行中/待确认/已完成/已取消），操作（取消/确认）
 * - 回收员：Tab（全部/已接单/上门中/待确认/已完成），操作（开始上门/完成称重）
 * - 根据 Pinia 中的 role 自动切换 API 和 Tab 配置
 */
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import request from '@/utils/request.js'

const userStore = useUserStore()

// 角色判断
const isUser = computed(() => userStore.isUser)
const isCollector = computed(() => userStore.isCollector)
const isInstitution = computed(() => userStore.isInstitution)

// ==================== 状态配置 ====================

// 用户端 Tab 配置
const userTabs = [
  { label: '全部', value: null },
  { label: '待接单', value: 0 },
  { label: '进行中', value: 'ongoing' },  // 合并状态 1+2
  { label: '待确认', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

// 回收员端 Tab 配置
const collectorTabs = [
  { label: '全部', value: null },
  { label: '已接单', value: 1 },
  { label: '上门中', value: 2 },
  { label: '待确认', value: 3 },
  { label: '已完成', value: 4 }
]

// 机构端 Tab 配置
const institutionTabs = [
  { label: '全部', value: null },
  { label: '已接收', value: 7 }
]

// 根据角色返回对应 Tab 配置
const currentTabs = computed(() => {
  if (isInstitution.value) return institutionTabs
  if (isCollector.value) return collectorTabs
  return userTabs
})

// 状态文字映射
const statusMap = {
  0: '待接单',
  1: '已接单',
  2: '上门中',
  3: '待确认',
  4: '已完成',
  5: '已取消',
  6: '异常',
  7: '机构已接收'
}

// 当前选中的 Tab
const currentTab = ref(null)

// 订单列表数据
const orderList = ref([])

// ==================== 数据加载 ====================

// 页面每次显示时加载订单列表
onShow(() => {
  loadOrders()
})

/**
 * 加载订单列表
 * 根据角色调用不同的 API
 */
async function loadOrders() {
  try {
    if (isInstitution.value) {
      await loadInstitutionOrders()
    } else if (isCollector.value) {
      await loadCollectorOrders()
    } else {
      await loadUserOrders()
    }
  } catch (e) {
    console.error('加载订单列表失败:', e)
  }
}

/**
 * 加载用户订单
 */
async function loadUserOrders() {
  if (currentTab.value === 'ongoing') {
    // "进行中"需要分别请求状态1和状态2，然后合并
    const [list1, list2] = await Promise.all([
      request({ url: '/api/user/order/list?status=1', loading: false }),
      request({ url: '/api/user/order/list?status=2', loading: false })
    ])
    const merged = [...(list1 || []), ...(list2 || [])]
    merged.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    orderList.value = merged
  } else {
    let url = '/api/user/order/list'
    if (currentTab.value !== null) {
      url += `?status=${currentTab.value}`
    }
    const data = await request({ url, loading: false })
    orderList.value = data || []
  }
}

/**
 * 加载回收员订单
 */
async function loadCollectorOrders() {
  let url = '/api/collector/order/list'
  if (currentTab.value !== null) {
    url += `?status=${currentTab.value}`
  }
  const data = await request({ url, loading: false })
  orderList.value = data || []
}

/**
 * 加载机构已接收订单
 */
async function loadInstitutionOrders() {
  let url = '/api/institution/order/list'
  if (currentTab.value !== null) {
    url += `?status=${currentTab.value}`
  }
  const data = await request({ url, loading: false })
  orderList.value = data || []
}

/**
 * 切换状态 Tab
 */
function switchTab(value) {
  currentTab.value = value
  loadOrders()
}

// ==================== 工具方法 ====================

/**
 * 解析地址快照 JSON，返回简短地址文本
 */
function parseAddress(snapshot) {
  if (!snapshot) return '未知地址'
  try {
    const addr = JSON.parse(snapshot)
    return `${addr.province || ''}${addr.city || ''}${addr.district || ''} ${addr.detailAddress || ''}`
  } catch {
    return '未知地址'
  }
}

/**
 * 解析衣物分类 JSON 数组
 */
function parseCategories(json) {
  if (!json) return '未填写'
  try {
    return JSON.parse(json).join('、')
  } catch {
    return json
  }
}

/**
 * 判断订单是否显示操作按钮
 */
function showActions(order) {
  // 机构端：无操作按钮
  if (isInstitution.value) return false
  if (isCollector.value) {
    // 回收员：已接单(1)可开始上门，上门中(2)可完成称重
    return order.status === 1 || order.status === 2
  }
  // 用户：待接单/已接单(0/1)可取消，待确认(3)可确认
  return order.status === 0 || order.status === 1 || order.status === 3
}

// ==================== 用户操作 ====================

/**
 * 跳转订单详情页（机构端不跳转，因为没有机构订单详情接口）
 */
function goDetail(id) {
  if (isInstitution.value) return
  uni.navigateTo({ url: `/pages/order/detail/detail?id=${id}` })
}

/**
 * 用户取消订单
 */
function handleCancel(order) {
  uni.showModal({
    title: '提示',
    content: '确定取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/user/order/${order.id}/cancel`,
            method: 'POST'
          })
          uni.showToast({ title: '取消成功', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error('取消订单失败:', e)
        }
      }
    }
  })
}

/**
 * 用户确认完成订单
 * 确认后等待机构扫码接收，届时发放积分
 */
function handleConfirm(order) {
  const weight = order.actualWeight || 0
  uni.showModal({
    title: '确认完成',
    content: `实际重量 ${weight}kg，确认后等待机构接收发放积分`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/user/order/${order.id}/confirm`,
            method: 'POST'
          })
          uni.showToast({ title: '确认成功', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error('确认订单失败:', e)
        }
      }
    }
  })
}

// ==================== 回收员操作 ====================

/**
 * 回收员开始上门
 */
function handlePickup(order) {
  uni.showModal({
    title: '确认出发',
    content: '确定开始上门回收吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/collector/order/${order.id}/pickup`,
            method: 'POST'
          })
          uni.showToast({ title: '已出发', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error('开始上门失败:', e)
        }
      }
    }
  })
}

/**
 * 回收员完成称重（需输入实际重量）
 */
function handleComplete(order) {
  // 弹出输入框让回收员填写实际重量
  uni.showModal({
    title: '完成称重',
    content: '',
    editable: true,
    placeholderText: '请输入实际称重重量(kg)',
    success: async (res) => {
      if (res.confirm) {
        const input = res.content && res.content.trim()
        if (!input) {
          uni.showToast({ title: '请输入重量', icon: 'none' })
          return
        }
        const weight = parseFloat(input)
        if (isNaN(weight) || weight <= 0) {
          uni.showToast({ title: '请输入有效的重量', icon: 'none' })
          return
        }
        try {
          await request({
            url: `/api/collector/order/${order.id}/complete`,
            method: 'POST',
            data: { actualWeight: weight }
          })
          uni.showToast({ title: '称重完成', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error('完成称重失败:', e)
        }
      }
    }
  })
}
</script>

<style scoped>
/* 页面容器 */
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

/* 状态 Tab 筛选栏 */
.tab-bar {
  display: flex;
  background: #fff;
  padding: 0 10rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 26rpx;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #07c160;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background: #07c160;
  border-radius: 2rpx;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  margin: 20rpx 24rpx 0;
  padding: 28rpx 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-no {
  font-size: 26rpx;
  color: #999;
}

/* 状态标签 */
.status-tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
}

.status-0 { color: #ff9800; background: #fff3e0; }
.status-1 { color: #2196f3; background: #e3f2fd; }
.status-2 { color: #9c27b0; background: #f3e5f5; }
.status-3 { color: #f44336; background: #ffebee; }
.status-4 { color: #4caf50; background: #e8f5e9; }
.status-5 { color: #9e9e9e; background: #f5f5f5; }
.status-6 { color: #f44336; background: #ffebee; }
.status-7 { color: #1565c0; background: #e3f2fd; }

/* 订单信息 */
.info-row {
  display: flex;
  margin-bottom: 10rpx;
  font-size: 26rpx;
  line-height: 1.5;
}

.info-label {
  width: 140rpx;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #333;
}

.info-value.highlight {
  color: #f44336;
  font-weight: bold;
}

/* 操作按钮区域 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  font-size: 24rpx;
  padding: 0 28rpx;
  height: 56rpx;
  line-height: 56rpx;
  border-radius: 28rpx;
  border: none;
}

.action-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.action-btn.confirm {
  background: #07c160;
  color: #fff;
}

.action-btn.pickup {
  background: #2196f3;
  color: #fff;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
