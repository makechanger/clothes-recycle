<template>
  <!-- 预约回收页（骨架，后续 Step 1.10 开发） -->
  <view class="page">
    <view class="placeholder">
      <text>预约回收页 - 待开发</text>
    </view>
    <!-- 底部导航栏：用户角色下"预约回收"是第2个tab（索引1） -->
    <custom-tabbar :current="1" />
  </view>
</template>

<script setup>
</script>

<style scoped>
.page { padding-bottom: 120rpx; }
.placeholder { text-align: center; padding: 200rpx 0; color: #999; font-size: 30rpx; }
</style>
