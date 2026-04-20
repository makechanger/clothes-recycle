<template>
  <!-- 积分流水明细页 -->
  <view class="page">
    <view v-if="list.length > 0" class="points-list">
      <view v-for="item in list" :key="item.id" class="points-card">
        <view class="card-left">
          <text class="desc">{{ item.description || typeMap[item.type] || '积分变动' }}</text>
          <text class="time">{{ item.createdAt }}</text>
        </view>
        <view class="card-right">
          <text :class="['amount', item.amount >= 0 ? 'plus' : 'minus']">
            {{ item.amount >= 0 ? '+' : '' }}{{ item.amount }}
          </text>
          <text class="balance">余额 {{ item.balanceAfter }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty">
      <text class="empty-text">暂无积分记录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const list = ref([])

const typeMap = {
  'EARN': '回收获取',
  'EXCHANGE': '积分兑换',
  'DEDUCT': '积分扣除',
  'ADJUST': '积分调整'
}

onShow(() => {
  loadList()
})

async function loadList() {
  try {
    const data = await request({ url: '/api/user/points/history', loading: false })
    list.value = data || []
  } catch (e) {
    console.error('加载积分流水失败:', e)
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx 24rpx;
}

.points-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  margin-bottom: 16rpx;
  padding: 28rpx 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-left {
  flex: 1;
}

.desc {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.time {
  font-size: 24rpx;
  color: #999;
}

.card-right {
  text-align: right;
  flex-shrink: 0;
  margin-left: 20rpx;
}

.amount {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 4rpx;
}

.amount.plus {
  color: #4caf50;
}

.amount.minus {
  color: #f44336;
}

.balance {
  font-size: 22rpx;
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
