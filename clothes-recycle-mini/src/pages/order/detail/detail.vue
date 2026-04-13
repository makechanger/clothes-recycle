<template>
  <!-- 订单详情页：用户和回收员共用，根据角色展示不同操作按钮 -->
  <view class="page" v-if="order">
    <!-- 订单状态横幅 -->
    <view :class="['status-banner', 'status-bg-' + order.status]">
      <text class="status-text">{{ statusMap[order.status] }}</text>
      <text class="status-desc">{{ currentStatusDesc }}</text>
    </view>

    <!-- 上门地址 -->
    <view class="section">
      <view class="section-title">上门地址</view>
      <view class="address-info" v-if="addressInfo">
        <view class="address-top">
          <text class="contact-name">{{ addressInfo.name }}</text>
          <text class="contact-phone">{{ addressInfo.phone }}</text>
        </view>
        <text class="address-detail">
          {{ addressInfo.province }}{{ addressInfo.city }}{{ addressInfo.district }} {{ addressInfo.detailAddress }}
        </text>
      </view>
    </view>

    <!-- 预约信息 -->
    <view class="section">
      <view class="section-title">预约信息</view>
      <view class="detail-row">
        <text class="detail-label">预约日期</text>
        <text class="detail-value">{{ order.appointmentDate }}</text>
      </view>
      <view class="detail-row">
        <text class="detail-label">预约时段</text>
        <text class="detail-value">{{ order.timeSlotStart }} - {{ order.timeSlotEnd }}</text>
      </view>
      <view class="detail-row">
        <text class="detail-label">衣物分类</text>
        <text class="detail-value">{{ parseCategories(order.clothesCategories) }}</text>
      </view>
      <view class="detail-row">
        <text class="detail-label">预估重量</text>
        <text class="detail-value">{{ order.estimatedWeight }}kg</text>
      </view>
      <view v-if="order.actualWeight" class="detail-row">
        <text class="detail-label">实际重量</text>
        <text class="detail-value highlight">{{ order.actualWeight }}kg</text>
      </view>
      <view v-if="order.pointsAwarded" class="detail-row">
        <text class="detail-label">获得积分</text>
        <text class="detail-value highlight">+{{ order.pointsAwarded }}</text>
      </view>
      <view v-if="order.remark" class="detail-row">
        <text class="detail-label">备注</text>
        <text class="detail-value">{{ order.remark }}</text>
      </view>
    </view>

    <!-- 衣物照片 -->
    <view v-if="photoList.length > 0" class="section">
      <view class="section-title">衣物照片</view>
      <view class="photo-list">
        <image
          v-for="(photo, index) in photoList"
          :key="index"
          :src="getPhotoUrl(photo)"
          class="photo-img"
          mode="aspectFill"
          @click="previewPhoto(index)"
        />
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="section">
      <view class="section-title">订单信息</view>
      <view class="detail-row">
        <text class="detail-label">订单编号</text>
        <text class="detail-value">{{ order.orderNo }}</text>
      </view>
      <view class="detail-row">
        <text class="detail-label">下单时间</text>
        <text class="detail-value">{{ order.createdAt }}</text>
      </view>
      <view v-if="order.acceptedAt" class="detail-row">
        <text class="detail-label">接单时间</text>
        <text class="detail-value">{{ order.acceptedAt }}</text>
      </view>
      <view v-if="order.completedAt" class="detail-row">
        <text class="detail-label">完成时间</text>
        <text class="detail-value">{{ order.completedAt }}</text>
      </view>
      <view v-if="order.cancelledAt" class="detail-row">
        <text class="detail-label">取消时间</text>
        <text class="detail-value">{{ order.cancelledAt }}</text>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view v-if="showActions" class="bottom-bar">
      <!-- ===== 用户操作 ===== -->
      <template v-if="isUser">
        <!-- 待接单/已接单 可取消 -->
        <button
          v-if="order.status === 0 || order.status === 1"
          class="action-btn cancel-btn"
          @click="handleCancel"
        >取消订单</button>
        <!-- 待确认 可确认完成 -->
        <button
          v-if="order.status === 3"
          class="action-btn confirm-btn"
          @click="handleConfirm"
        >确认完成</button>
      </template>

      <!-- ===== 回收员操作 ===== -->
      <template v-if="isCollector">
        <!-- 待接单 可接单 -->
        <button
          v-if="order.status === 0"
          class="action-btn accept-btn"
          @click="handleAccept"
        >接单</button>
        <!-- 已接单 可开始上门 -->
        <button
          v-if="order.status === 1"
          class="action-btn pickup-btn"
          @click="handlePickup"
        >开始上门</button>
        <!-- 上门中 可完成称重 -->
        <button
          v-if="order.status === 2"
          class="action-btn confirm-btn"
          @click="handleComplete"
        >完成称重</button>
      </template>
    </view>
  </view>
</template>

<script setup>
/**
 * 订单详情页（用户 + 回收员共用）
 * - 通过 URL 参数 id 获取订单详情
 * - 根据角色调用不同 API 获取数据
 * - 用户操作：取消订单（状态0/1）、确认完成（状态3）
 * - 回收员操作：接单（状态0）、开始上门（状态1）、完成称重（状态2）
 */
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import request from '@/utils/request.js'
import { BASE_URL } from '@/utils/request.js'

const userStore = useUserStore()

// 角色判断
const isUser = computed(() => userStore.isUser)
const isCollector = computed(() => userStore.isCollector)

// ==================== 状态配置 ====================

// 状态文字映射
const statusMap = {
  0: '待接单',
  1: '已接单',
  2: '上门中',
  3: '待确认',
  4: '已完成',
  5: '已取消',
  6: '异常'
}

// 用户视角的状态描述
const userStatusDesc = {
  0: '等待回收员接单，请耐心等待',
  1: '回收员已接单，即将上门',
  2: '回收员正在上门途中',
  3: '回收员已完成称重，请确认重量',
  4: '订单已完成，积分已发放',
  5: '订单已取消',
  6: '订单出现异常，请联系客服'
}

// 回收员视角的状态描述
const collectorStatusDesc = {
  0: '该订单等待接单，确认后即可开始',
  1: '您已接单，请尽快出发上门',
  2: '正在上门中，完成后请称重',
  3: '已完成称重，等待用户确认',
  4: '订单已完成',
  5: '订单已取消',
  6: '订单出现异常'
}

// 根据角色返回对应的状态描述
const currentStatusDesc = computed(() => {
  if (!order.value) return ''
  const descMap = isCollector.value ? collectorStatusDesc : userStatusDesc
  return descMap[order.value.status] || ''
})

// ==================== 数据 ====================

// 订单详情
const order = ref(null)

// 当前订单 ID（用于刷新）
const orderId = ref(null)

// 解析后的地址信息
const addressInfo = ref(null)

// 解析后的照片列表
const photoList = ref([])

// 是否显示操作按钮（根据角色和状态判断）
const showActions = computed(() => {
  if (!order.value) return false
  if (isCollector.value) {
    // 回收员：待接单(0)可接单，已接单(1)可上门，上门中(2)可称重
    return order.value.status === 0 || order.value.status === 1 || order.value.status === 2
  }
  // 用户：待接单/已接单(0/1)可取消，待确认(3)可确认
  return order.value.status === 0 || order.value.status === 1 || order.value.status === 3
})

// ==================== 页面加载 ====================

onLoad((options) => {
  if (options.id) {
    orderId.value = options.id
    loadOrderDetail(options.id)
  }
})

/**
 * 加载订单详情
 * 根据角色调用不同的 API
 */
async function loadOrderDetail(id) {
  try {
    // 回收员调用回收员接口，用户调用用户接口
    const url = isCollector.value
      ? `/api/collector/order/${id}`
      : `/api/user/order/${id}`

    const data = await request({ url })
    order.value = data

    // 解析地址快照 JSON
    if (data.addressSnapshot) {
      try {
        addressInfo.value = JSON.parse(data.addressSnapshot)
      } catch {
        addressInfo.value = null
      }
    }

    // 解析照片列表 JSON
    if (data.photos) {
      try {
        photoList.value = JSON.parse(data.photos)
      } catch {
        photoList.value = []
      }
    }
  } catch (e) {
    console.error('加载订单详情失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// ==================== 工具方法 ====================

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
 * 获取照片完整 URL
 */
function getPhotoUrl(photo) {
  return photo.startsWith('http') ? photo : BASE_URL + photo
}

/**
 * 预览照片（全屏查看）
 */
function previewPhoto(index) {
  const urls = photoList.value.map(p => getPhotoUrl(p))
  uni.previewImage({
    current: urls[index],
    urls
  })
}

// ==================== 用户操作 ====================

/**
 * 用户取消订单
 */
function handleCancel() {
  uni.showModal({
    title: '提示',
    content: '确定取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/user/order/${order.value.id}/cancel`,
            method: 'POST'
          })
          uni.showToast({ title: '取消成功', icon: 'success' })
          // 重新加载详情
          loadOrderDetail(orderId.value)
        } catch (e) {
          console.error('取消订单失败:', e)
        }
      }
    }
  })
}

/**
 * 用户确认完成订单（确认称重结果，发放积分）
 */
function handleConfirm() {
  const weight = order.value.actualWeight || 0
  const points = Math.floor(weight * 10)
  uni.showModal({
    title: '确认完成',
    content: `实际重量 ${weight}kg，确认后将获得 ${points} 积分`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/user/order/${order.value.id}/confirm`,
            method: 'POST'
          })
          uni.showToast({ title: '确认成功，积分已发放', icon: 'success' })
          // 重新加载详情
          loadOrderDetail(orderId.value)
        } catch (e) {
          console.error('确认订单失败:', e)
        }
      }
    }
  })
}

// ==================== 回收员操作 ====================

/**
 * 回收员接单（二次确认）
 */
function handleAccept() {
  uni.showModal({
    title: '确认接单',
    content: `确定接受该订单吗？\n预约时间：${order.value.appointmentDate} ${order.value.timeSlotStart}-${order.value.timeSlotEnd}`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/collector/order/${order.value.id}/accept`,
            method: 'POST'
          })
          uni.showToast({ title: '接单成功', icon: 'success' })
          // 重新加载详情
          loadOrderDetail(orderId.value)
        } catch (e) {
          console.error('接单失败:', e)
        }
      }
    }
  })
}

/**
 * 回收员开始上门（确认出发）
 */
function handlePickup() {
  uni.showModal({
    title: '确认出发',
    content: '确定开始上门回收吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/collector/order/${order.value.id}/pickup`,
            method: 'POST'
          })
          uni.showToast({ title: '已出发', icon: 'success' })
          // 重新加载详情
          loadOrderDetail(orderId.value)
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
function handleComplete() {
  // 弹出输入框让回收员填写实际重量
  uni.showModal({
    title: '完成称重',
    content: '请输入实际称重重量(kg)',
    editable: true,
    placeholderText: '例如: 5.5',
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
            url: `/api/collector/order/${order.value.id}/complete`,
            method: 'POST',
            data: { actualWeight: weight }
          })
          uni.showToast({ title: '称重完成', icon: 'success' })
          // 重新加载详情
          loadOrderDetail(orderId.value)
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
  padding-bottom: 160rpx;
}

/* 状态横幅 */
.status-banner {
  padding: 40rpx 30rpx;
  color: #fff;
}

.status-text {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.status-desc {
  font-size: 26rpx;
  opacity: 0.9;
}

/* 不同状态的横幅背景色 */
.status-bg-0 { background: linear-gradient(135deg, #ff9800, #ffc107); }
.status-bg-1 { background: linear-gradient(135deg, #2196f3, #42a5f5); }
.status-bg-2 { background: linear-gradient(135deg, #9c27b0, #ba68c8); }
.status-bg-3 { background: linear-gradient(135deg, #f44336, #ef5350); }
.status-bg-4 { background: linear-gradient(135deg, #4caf50, #66bb6a); }
.status-bg-5 { background: linear-gradient(135deg, #9e9e9e, #bdbdbd); }
.status-bg-6 { background: linear-gradient(135deg, #f44336, #e53935); }

/* 区块样式 */
.section {
  background: #fff;
  margin: 20rpx 24rpx 0;
  padding: 24rpx 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 地址信息 */
.address-top {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.contact-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-right: 16rpx;
}

.contact-phone {
  font-size: 26rpx;
  color: #666;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

/* 详情行 */
.detail-row {
  display: flex;
  padding: 12rpx 0;
  font-size: 26rpx;
  line-height: 1.5;
}

.detail-label {
  width: 150rpx;
  color: #999;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #333;
}

.detail-value.highlight {
  color: #f44336;
  font-weight: bold;
}

/* 照片列表 */
.photo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.photo-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
}

/* 底部操作按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 12rpx;
  font-size: 30rpx;
  text-align: center;
  border: none;
}

/* 取消按钮（灰色） */
.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

/* 确认/称重按钮（绿色） */
.confirm-btn {
  background: #07c160;
  color: #fff;
}

/* 接单按钮（绿色） */
.accept-btn {
  background: #07c160;
  color: #fff;
}

/* 开始上门按钮（蓝色） */
.pickup-btn {
  background: #2196f3;
  color: #fff;
}
</style>
