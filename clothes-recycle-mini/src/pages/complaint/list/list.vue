<template>
  <!-- 我的申诉列表页 -->
  <view class="page">
    <view v-if="list.length > 0" class="complaint-list">
      <view v-for="item in list" :key="item.id" class="complaint-card">
        <!-- 头部：申诉类型 + 状态 -->
        <view class="card-header">
          <text class="type-tag">{{ typeMap[item.type] || item.type }}</text>
          <text :class="['status-tag', item.status === 1 ? 'handled' : 'pending']">
            {{ item.status === 1 ? '已处理' : '待处理' }}
          </text>
        </view>

        <!-- 申诉描述 -->
        <view class="card-body">
          <text class="desc">{{ item.description }}</text>
        </view>

        <!-- 处理结果（已处理时显示） -->
        <view v-if="item.status === 1" class="result-section">
          <view v-if="item.action" class="info-row">
            <text class="info-label">处理动作</text>
            <text class="info-value">{{ actionMap[item.action] || item.action }}</text>
          </view>
          <view v-if="item.adminRemark" class="info-row">
            <text class="info-label">管理员备注</text>
            <text class="info-value">{{ item.adminRemark }}</text>
          </view>
          <view v-if="item.handledAt" class="info-row">
            <text class="info-label">处理时间</text>
            <text class="info-value">{{ item.handledAt }}</text>
          </view>
        </view>

        <!-- 底部：提交时间 -->
        <view class="card-footer">
          <text class="time">提交时间：{{ item.createdAt }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty">
      <text class="empty-text">暂无申诉记录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const list = ref([])

// 申诉类型映射
const typeMap = {
  'WEIGHT_DISPUTE': '重量争议',
  'SERVICE_ISSUE': '服务问题',
  'ITEM_LOST': '物品丢失',
  'OTHER': '其他'
}

// 处理动作映射
const actionMap = {
  'ADD_POINTS': '增加积分',
  'DEDUCT_POINTS': '扣减积分',
  'MARK_ABNORMAL': '标记订单异常'
}

onShow(() => {
  loadList()
})

async function loadList() {
  try {
    const data = await request({ url: '/api/user/complaint/list', loading: false })
    list.value = data || []
  } catch (e) {
    console.error('加载申诉列表失败:', e)
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx 24rpx;
}

.complaint-card {
  background: #fff;
  margin-bottom: 20rpx;
  padding: 28rpx 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.type-tag {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.status-tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
}

.status-tag.pending {
  color: #ff9800;
  background: #fff3e0;
}

.status-tag.handled {
  color: #4caf50;
  background: #e8f5e9;
}

.card-body {
  margin-bottom: 16rpx;
}

.desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.result-section {
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  margin-bottom: 8rpx;
  font-size: 26rpx;
  line-height: 1.5;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 160rpx;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #333;
}

.card-footer {
  padding-top: 12rpx;
  border-top: 1rpx solid #f0f0f0;
}

.time {
  font-size: 24rpx;
  color: #999;
}

.empty {
  text-align: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
