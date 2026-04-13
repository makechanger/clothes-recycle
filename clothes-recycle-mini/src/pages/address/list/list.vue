<template>
  <!-- 地址列表页：展示当前用户所有收货地址，支持新增/编辑/删除/设默认 -->
  <view class="page">
    <!-- 地址列表 -->
    <view v-if="addressList.length > 0" class="address-list">
      <view
        v-for="item in addressList"
        :key="item.id"
        class="address-card"
        @click="handleEdit(item.id)"
      >
        <!-- 联系人信息 -->
        <view class="address-top">
          <text class="contact-name">{{ item.name }}</text>
          <text class="contact-phone">{{ item.phone }}</text>
          <text v-if="item.isDefault === 1" class="default-tag">默认</text>
        </view>
        <!-- 详细地址 -->
        <view class="address-detail">
          <text>{{ item.province }}{{ item.city }}{{ item.district }} {{ item.detailAddress }}</text>
        </view>
        <!-- 操作按钮 -->
        <view class="address-actions" @click.stop="">
          <text
            v-if="item.isDefault !== 1"
            class="action-btn"
            @click.stop="handleSetDefault(item.id)"
          >设为默认</text>
          <text class="action-btn" @click.stop="handleEdit(item.id)">编辑</text>
          <text class="action-btn delete" @click.stop="handleDelete(item.id)">删除</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty">
      <text class="empty-text">暂无地址，请添加</text>
    </view>

    <!-- 底部新增按钮 -->
    <view class="bottom-bar">
      <button class="add-btn" @click="handleAdd">+ 新增地址</button>
    </view>
  </view>
</template>

<script setup>
/**
 * 地址列表页
 * - 展示当前用户的所有地址，默认地址排在最前
 * - 支持新增、编辑、删除、设为默认操作
 * - 每次页面显示时重新加载列表（从编辑页返回后刷新）
 */
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

// 地址列表数据
const addressList = ref([])

// 页面每次显示时加载地址列表（包括从编辑页返回）
onShow(() => {
  loadAddressList()
})

/**
 * 加载地址列表
 */
async function loadAddressList() {
  try {
    const data = await request({ url: '/api/user/address/list' })
    addressList.value = data || []
  } catch (e) {
    console.error('加载地址列表失败:', e)
  }
}

/**
 * 跳转新增地址页
 */
function handleAdd() {
  uni.navigateTo({ url: '/pages/address/edit/edit' })
}

/**
 * 跳转编辑地址页，携带地址ID
 */
function handleEdit(id) {
  uni.navigateTo({ url: `/pages/address/edit/edit?id=${id}` })
}

/**
 * 删除地址（二次确认）
 */
function handleDelete(id) {
  uni.showModal({
    title: '提示',
    content: '确定删除该地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/api/user/address/${id}/delete`,
            method: 'POST'
          })
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadAddressList()
        } catch (e) {
          console.error('删除地址失败:', e)
        }
      }
    }
  })
}

/**
 * 设为默认地址
 */
async function handleSetDefault(id) {
  try {
    await request({
      url: `/api/user/address/${id}/setDefault`,
      method: 'POST'
    })
    uni.showToast({ title: '设置成功', icon: 'success' })
    loadAddressList()
  } catch (e) {
    console.error('设置默认地址失败:', e)
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}

/* 地址卡片 */
.address-card {
  background: #fff;
  margin: 20rpx 24rpx 0;
  padding: 28rpx 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.address-top {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.contact-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-right: 20rpx;
}

.contact-phone {
  font-size: 28rpx;
  color: #666;
}

.default-tag {
  font-size: 22rpx;
  color: #07c160;
  border: 1rpx solid #07c160;
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
  margin-left: 16rpx;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

/* 操作按钮区域 */
.address-actions {
  display: flex;
  justify-content: flex-end;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 16rpx;
}

.action-btn {
  font-size: 24rpx;
  color: #07c160;
  margin-left: 32rpx;
  padding: 4rpx 0;
}

.action-btn.delete {
  color: #e53935;
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

/* 底部新增按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.add-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  color: #fff;
  border-radius: 12rpx;
  font-size: 30rpx;
  text-align: center;
  border: none;
}
</style>
