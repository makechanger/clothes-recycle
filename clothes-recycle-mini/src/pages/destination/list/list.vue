<template>
  <view class="page">
    <view class="section-title">待分配去向的订单</view>

    <view v-if="orders.length > 0">
      <view v-for="order in orders" :key="order.id" class="order-card">
        <!-- 订单头部 -->
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="status-tag">机构已接收</text>
        </view>

        <!-- 订单信息 -->
        <view class="order-info">
          <view class="info-row">
            <text class="info-label">衣物分类</text>
            <text class="info-value">{{ parseCategories(order.clothesCategories) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">实际重量</text>
            <text class="info-value">{{ order.actualWeight }}kg</text>
          </view>
          <view class="info-row">
            <text class="info-label">获得积分</text>
            <text class="info-value">{{ order.pointsAwarded }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="order-actions">
          <button class="assign-btn" @click="openAssignPopup(order)">分配去向</button>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-text">暂无待分配去向的订单</text>
    </view>

    <!-- 分配去向弹窗 -->
    <view v-if="showPopup" class="popup-mask" @click.stop="showPopup = false">
      <view class="popup-content" @click.stop>
        <view class="popup-title">分配衣物去向</view>
        <view class="popup-order-no">订单号：{{ currentOrder.orderNo }}</view>

        <!-- 去向类型（radio 选择） -->
        <view class="form-item">
          <text class="form-label">去向类型</text>
          <radio-group @change="popupForm.typeIndex = Number($event.detail.value)">
            <label v-for="(opt, idx) in typeOptions" :key="opt.value" class="radio-option">
              <radio :value="String(idx)" :checked="popupForm.typeIndex === idx" color="#2e7d32" />
              <text class="radio-text">{{ opt.label }}</text>
            </label>
          </radio-group>
        </view>

        <!-- 去向描述 -->
        <view class="form-item">
          <text class="form-label">去向描述</text>
          <textarea
            v-model="popupForm.desc"
            class="popup-textarea"
            placeholder="如：捐赠至云南山区小学"
            maxlength="200"
          />
        </view>

        <view class="popup-btns">
          <button class="popup-btn cancel" @click="showPopup = false">取消</button>
          <button class="popup-btn submit" @click="handleAssign">确认分配</button>
        </view>
      </view>
    </view>

    <custom-tabbar :current="2" />
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const typeOptions = [
  { label: '捐赠', value: 'DONATION' },
  { label: '再生利用', value: 'RECYCLE' },
  { label: '环保处理', value: 'ENVIRONMENTAL' }
]
const orders = ref([])
const showPopup = ref(false)
const currentOrder = ref(null)
const popupForm = reactive({ typeIndex: 0, desc: '' })

onShow(() => {
  loadOrders()
})

async function loadOrders() {
  try {
    const data = await request({ url: '/api/institution/destination/unassigned-orders', loading: false })
    orders.value = data || []
  } catch (e) {
    console.error('加载待分配订单失败:', e)
  }
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/order/detail/detail?id=${id}` })
}

function openAssignPopup(order) {
  currentOrder.value = order
  popupForm.typeIndex = 0
  popupForm.desc = ''
  showPopup.value = true
}

async function handleAssign() {
  const desc = popupForm.desc.trim()
  if (!desc) {
    uni.showToast({ title: '请填写去向描述', icon: 'none' })
    return
  }
  const destinationType = typeOptions[popupForm.typeIndex].value
  try {
    await request({
      url: '/api/institution/destination/assign',
      method: 'POST',
      data: {
        orderId: currentOrder.value.id,
        destinationType,
        description: desc
      }
    })
    uni.showToast({ title: '分配成功', icon: 'success' })
    showPopup.value = false
    loadOrders()
  } catch (e) {
    console.error('分配去向失败:', e)
  }
}

function parseCategories(json) {
  if (!json) return '未填写'
  try {
    return JSON.parse(json).join('、')
  } catch {
    return json
  }
}
</script>

<style scoped>
.page {
  padding: 20rpx 30rpx 140rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-no {
  font-size: 26rpx;
  color: #999;
}

.status-tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
  color: #1565c0;
  background: #e3f2fd;
}

.order-info {
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  margin-bottom: 8rpx;
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

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.detail-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 40rpx;
  background: #f5f5f5;
  color: #666;
  border-radius: 32rpx;
  font-size: 26rpx;
  border: none;
}

.assign-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 40rpx;
  background: #2e7d32;
  color: #fff;
  border-radius: 32rpx;
  font-size: 26rpx;
  border: none;
}

.empty {
  text-align: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 弹窗样式 */
.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.popup-content {
  width: 85%;
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 12rpx;
}

.popup-order-no {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 20rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.radio-option {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
}

.radio-text {
  font-size: 28rpx;
  color: #333;
  margin-left: 12rpx;
}

.popup-textarea {
  width: 100%;
  height: 160rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}

.popup-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.popup-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  text-align: center;
  border: none;
}

.popup-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.popup-btn.submit {
  background: #2e7d32;
  color: #fff;
}
</style>
