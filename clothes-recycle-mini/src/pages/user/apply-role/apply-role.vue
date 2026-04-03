<template>
  <!-- 资质申请页面：普通用户可申请成为回收员或机构 -->
  <view class="page">
    <!-- 如果已有待审核的申请，显示提示 -->
    <view v-if="pendingApplication" class="pending-tip">
      <text class="tip-icon">⏳</text>
      <text class="tip-text">您有一条"{{ pendingApplication.applyRole === 'COLLECTOR' ? '回收员' : '机构' }}"申请正在审核中，请耐心等待</text>
    </view>

    <!-- 申请表单（有待审核申请时隐藏） -->
    <view v-if="!pendingApplication" class="form">
      <!-- 选择申请角色 -->
      <view class="form-item">
        <text class="label">申请角色</text>
        <view class="role-picker">
          <view
            class="role-option"
            :class="{ active: applyRole === 'COLLECTOR' }"
            @click="applyRole = 'COLLECTOR'"
          >
            <text class="role-name">回收员</text>
            <text class="role-desc">个人回收员，上门回收衣物</text>
          </view>
          <view
            class="role-option"
            :class="{ active: applyRole === 'INSTITUTION' }"
            @click="applyRole = 'INSTITUTION'"
          >
            <text class="role-name">机构</text>
            <text class="role-desc">回收机构，批量处理衣物</text>
          </view>
        </view>
      </view>

      <!-- 回收员申请表单 -->
      <template v-if="applyRole === 'COLLECTOR'">
        <view class="form-item">
          <text class="label">真实姓名 <text class="required">*</text></text>
          <input
            v-model="form.name"
            class="input-field"
            placeholder="请输入您的真实姓名"
          />
        </view>
        <view class="form-item">
          <text class="label">身份证照片 <text class="required">*</text></text>
          <view class="upload-area" @click="chooseImage('idCardPhoto')">
            <image v-if="form.idCardPhoto" :src="form.idCardPhoto" class="preview-img" mode="aspectFit" />
            <view v-else class="upload-placeholder">
              <text class="upload-icon">+</text>
              <text class="upload-text">上传身份证照片</text>
            </view>
          </view>
        </view>
      </template>

      <!-- 机构申请表单 -->
      <template v-if="applyRole === 'INSTITUTION'">
        <view class="form-item">
          <text class="label">机构名称 <text class="required">*</text></text>
          <input
            v-model="form.name"
            class="input-field"
            placeholder="请输入机构名称"
          />
        </view>
        <view class="form-item">
          <text class="label">机构地址 <text class="required">*</text></text>
          <input
            v-model="form.address"
            class="input-field"
            placeholder="请输入机构地址"
          />
        </view>
        <view class="form-item">
          <text class="label">联系人 <text class="required">*</text></text>
          <input
            v-model="form.contactPerson"
            class="input-field"
            placeholder="请输入联系人姓名"
          />
        </view>
      </template>

      <!-- 提交按钮 -->
      <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中...' : '提交申请' }}
      </button>
    </view>
    <!-- 我的申请记录 -->
    <view v-if="applications.length > 0" class="history">
      <text class="history-title">申请记录</text>
      <view v-for="item in applications" :key="item.id" class="history-item">
        <view class="history-header">
          <text class="history-role">{{ item.applyRole === 'COLLECTOR' ? '回收员' : '机构' }}</text>
          <text class="history-status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>
        <text class="history-name">{{ item.name }}</text>
        <text v-if="item.status === 2 && item.rejectReason" class="reject-reason">拒绝原因：{{ item.rejectReason }}</text>
        <text class="history-time">{{ item.createdAt }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
/**
 * 资质申请页面
 * - 普通用户可以申请成为回收员或机构
 * - 回收员需填写：姓名、身份证照片
 * - 机构需填写：名称、地址、联系人
 * - 有待审核申请时不能重复提交，需等待审核结果
 * - 提交后等待管理员审批，页面下方显示历史申请记录
 */
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

// 申请角色：COLLECTOR 或 INSTITUTION
const applyRole = ref('COLLECTOR')

// 表单数据
const form = reactive({
  name: '',
  idCardPhoto: '',
  address: '',
  contactPerson: ''
})

// 提交状态
const submitting = ref(false)

// 申请记录列表
const applications = ref([])

// 当前待审核的申请（用于提示）
const pendingApplication = ref(null)

/** 页面显示时加载申请记录 */
onShow(() => {
  loadApplications()
})

/** 加载我的申请记录 */
async function loadApplications() {
  try {
    const res = await request({
      url: '/api/user/myApplications',
      method: 'GET'
    })
    applications.value = res
    // 检查是否有待审核的申请
    pendingApplication.value = res.find(item => item.status === 0) || null
  } catch (e) {
    console.error('加载申请记录失败:', e)
  }
}

/**
 * 选择图片并上传
 * 目前使用本地临时路径作为占位，后续接入文件上传接口后替换
 */
function chooseImage(field) {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // 暂时使用本地临时路径，后续对接文件上传接口
      form[field] = res.tempFilePaths[0]
      uni.showToast({ title: '图片已选择', icon: 'success' })
    }
  })
}

/** 提交资质申请 */
async function handleSubmit() {
  // 1. 前端校验
  if (applyRole.value === 'COLLECTOR') {
    if (!form.name.trim()) {
      uni.showToast({ title: '请输入真实姓名', icon: 'none' })
      return
    }
    if (!form.idCardPhoto) {
      uni.showToast({ title: '请上传身份证照片', icon: 'none' })
      return
    }
  } else {
    if (!form.name.trim()) {
      uni.showToast({ title: '请输入机构名称', icon: 'none' })
      return
    }
    if (!form.address.trim()) {
      uni.showToast({ title: '请输入机构地址', icon: 'none' })
      return
    }
    if (!form.contactPerson.trim()) {
      uni.showToast({ title: '请输入联系人', icon: 'none' })
      return
    }
  }

  submitting.value = true
  try {
    // 2. 构造请求数据
    const data = {
      applyRole: applyRole.value,
      name: form.name.trim()
    }
    if (applyRole.value === 'COLLECTOR') {
      data.idCardPhoto = form.idCardPhoto
    } else {
      data.address = form.address.trim()
      data.contactPerson = form.contactPerson.trim()
    }

    // 3. 调用申请接口
    await request({
      url: '/api/user/applyRole',
      method: 'POST',
      data
    })

    uni.showToast({ title: '申请已提交', icon: 'success' })

    // 4. 清空表单，刷新记录
    resetForm()
    loadApplications()
  } catch (e) {
    console.error('提交申请失败:', e)
  } finally {
    submitting.value = false
  }
}

/** 重置表单 */
function resetForm() {
  form.name = ''
  form.idCardPhoto = ''
  form.address = ''
  form.contactPerson = ''
}

/** 申请状态文字 */
function statusText(status) {
  const map = { 0: '审核中', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

/** 申请状态样式类 */
function statusClass(status) {
  const map = { 0: 'status-pending', 1: 'status-approved', 2: 'status-rejected' }
  return map[status] || ''
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  padding-bottom: 60rpx;
}

/* 待审核提示 */
.pending-tip {
  display: flex;
  align-items: center;
  background: #fff8e1;
  border: 2rpx solid #ffe082;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 30rpx;
}

.tip-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #f57f17;
  flex: 1;
}

/* 表单区域 */
.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.required {
  color: #e53935;
}

.input-field {
  width: 100%;
  height: 88rpx;
  background: #f8f8f8;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

/* 角色选择器 */
.role-picker {
  display: flex;
  gap: 20rpx;
}

.role-option {
  flex: 1;
  background: #f8f8f8;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  padding: 24rpx;
  text-align: center;
}

.role-option.active {
  background: #e8f5e9;
  border-color: #07c160;
}

.role-name {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.role-desc {
  display: block;
  font-size: 22rpx;
  color: #999;
}

.role-option.active .role-name {
  color: #07c160;
}

/* 图片上传区域 */
.upload-area {
  width: 300rpx;
  height: 200rpx;
  border: 2rpx dashed #ccc;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 60rpx;
  color: #ccc;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.preview-img {
  width: 100%;
  height: 100%;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  text-align: center;
  border: none;
  margin-top: 20rpx;
}

.submit-btn[disabled] {
  background: #b2dfdb;
}

/* 申请记录 */
.history {
  margin-top: 40rpx;
}

.history-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.history-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.history-role {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.history-status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.status-pending {
  background: #fff8e1;
  color: #f57f17;
}

.status-approved {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-rejected {
  background: #ffebee;
  color: #c62828;
}

.history-name {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.reject-reason {
  display: block;
  font-size: 24rpx;
  color: #e53935;
  margin-bottom: 8rpx;
}

.history-time {
  display: block;
  font-size: 22rpx;
  color: #999;
}
</style>
