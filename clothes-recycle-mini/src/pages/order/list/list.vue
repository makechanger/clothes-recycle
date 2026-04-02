<template>
  <!-- 订单列表页（骨架，后续 Step 1.12 开发） -->
  <view class="page">
    <view class="placeholder">
      <text>订单列表页 - 待开发</text>
    </view>
    <custom-tabbar :current="userStore.isUser ? 2 : 1" />
  </view>
</template>

<script setup>
import { useUserStore } from '@/store/user.js'
const userStore = useUserStore()
</script>

<style scoped>
.page { padding-bottom: 120rpx; }
.placeholder { text-align: center; padding: 200rpx 0; color: #999; font-size: 30rpx; }
</style>
