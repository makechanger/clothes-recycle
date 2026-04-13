<template>
  <!-- 预约回收页：用户填写回收信息并提交订单 -->
  <view class="page">
    <!-- 选择上门地址 -->
    <view class="section">
      <view class="section-title">上门地址</view>
      <view v-if="selectedAddress" class="address-card" @click="goSelectAddress">
        <view class="address-info">
          <text class="address-name">{{ selectedAddress.name }}</text>
          <text class="address-phone">{{ selectedAddress.phone }}</text>
        </view>
        <view class="address-detail-text">
          {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }} {{ selectedAddress.detailAddress }}
        </view>
        <text class="arrow">></text>
      </view>
      <view v-else class="address-empty" @click="goSelectAddress">
        <text class="empty-hint">请选择上门地址</text>
        <text class="arrow">></text>
      </view>
    </view>

    <!-- 预约时间 -->
    <view class="section">
      <view class="section-title">预约时间</view>
      <!-- 预约日期 -->
      <picker mode="date" :start="minDate" :end="maxDate" @change="onDateChange">
        <view class="form-item">
          <text class="label">预约日期</text>
          <text :class="['value', { placeholder: !form.appointmentDate }]">
            {{ form.appointmentDate || '请选择日期' }}
          </text>
          <text class="arrow">></text>
        </view>
      </picker>
      <!-- 预约时间段 -->
      <picker mode="multiSelector" :range="timeSlotRange" :value="timeSlotIndex" @change="onTimeSlotChange" @columnchange="onTimeSlotColumnChange">
        <view class="form-item">
          <text class="label">预约时段</text>
          <text :class="['value', { placeholder: !timeSlotText }]">
            {{ timeSlotText || '请选择时间段' }}
          </text>
          <text class="arrow">></text>
        </view>
      </picker>
    </view>

    <!-- 衣物信息 -->
    <view class="section">
      <view class="section-title">衣物信息</view>
      <!-- 衣物分类（多选） -->
      <view class="form-item column">
        <text class="label">衣物分类</text>
        <view class="category-list">
          <view
            v-for="item in categoryOptions"
            :key="item"
            :class="['category-tag', { active: selectedCategories.includes(item) }]"
            @click="toggleCategory(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>
      <!-- 预估重量 -->
      <view class="form-item">
        <text class="label">预估重量</text>
        <input
          v-model="form.estimatedWeight"
          class="input"
          type="digit"
          placeholder="请输入预估重量"
        />
        <text class="unit">kg</text>
      </view>
    </view>

    <!-- 照片上传 -->
    <view class="section">
      <view class="section-title">衣物照片（选填，最多6张）</view>
      <view class="photo-list">
        <!-- 已上传的照片 -->
        <view v-for="(photo, index) in photoList" :key="index" class="photo-item">
          <image :src="photo" class="photo-img" mode="aspectFill" @click="previewPhoto(index)" />
          <view class="photo-delete" @click="removePhoto(index)">×</view>
        </view>
        <!-- 添加照片按钮 -->
        <view v-if="photoList.length < 6" class="photo-add" @click="choosePhoto">
          <text class="photo-add-icon">+</text>
          <text class="photo-add-text">添加照片</text>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="section">
      <view class="section-title">备注（选填）</view>
      <textarea
        v-model="form.remark"
        class="remark-input"
        placeholder="请输入备注信息，如特殊要求等"
        maxlength="200"
        :auto-height="true"
      />
    </view>

    <!-- 提交按钮 -->
    <view class="bottom-bar">
      <button class="submit-btn" @click="handleSubmit">提交预约</button>
    </view>

    <!-- 底部导航栏：用户角色下"预约回收"是第2个tab（索引1） -->
    <custom-tabbar :current="1" />
  </view>
</template>

<script setup>
/**
 * 预约回收页（创建订单）
 * - 选择上门地址（从地址列表页选择，通过事件通道传回）
 * - 选择预约日期和时间段
 * - 选择衣物分类（多选标签）
 * - 填写预估重量
 * - 上传衣物照片（前端 canvas 压缩至 ≤1MB 后上传）
 * - 填写备注
 * - 提交订单
 */
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { uploadFile, BASE_URL } from '@/utils/request.js'

// ==================== 地址相关 ====================

// 当前选中的地址
const selectedAddress = ref(null)

// 地址列表（用于选择）
const addressList = ref([])

/**
 * 页面每次显示时加载地址列表
 * 如果还没选地址，自动选中默认地址
 */
onShow(() => {
  loadAddressList()
})

/**
 * 加载用户地址列表，自动选中默认地址
 */
async function loadAddressList() {
  try {
    const data = await request({ url: '/api/user/address/list', loading: false })
    addressList.value = data || []
    // 如果还没选地址，自动选中默认地址或第一个地址
    if (!selectedAddress.value && addressList.value.length > 0) {
      const defaultAddr = addressList.value.find(a => a.isDefault === 1)
      selectedAddress.value = defaultAddr || addressList.value[0]
    }
  } catch (e) {
    console.error('加载地址列表失败:', e)
  }
}

/**
 * 跳转地址列表页选择地址
 * 使用 uni.$once 事件通道接收选中的地址
 */
function goSelectAddress() {
  // 注册一次性事件，接收地址列表页传回的地址
  uni.$once('selectAddress', (address) => {
    selectedAddress.value = address
  })
  uni.navigateTo({ url: '/pages/address/list/list?mode=select' })
}

// ==================== 预约时间相关 ====================

// 表单数据
const form = ref({
  appointmentDate: '',
  estimatedWeight: '',
  remark: ''
})

// 日期范围：今天到30天后
const today = new Date()
const minDate = formatDate(today)
const futureDate = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000)
const maxDate = formatDate(futureDate)

/**
 * 格式化日期为 yyyy-MM-dd
 */
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/**
 * 日期选择回调
 */
function onDateChange(e) {
  form.value.appointmentDate = e.detail.value
}

// 时间段选项：开始时间和结束时间
const startHours = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00']
const endHours = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00']

// 多列选择器的数据源
const timeSlotRange = ref([startHours, endHours])
// 多列选择器当前选中索引
const timeSlotIndex = ref([0, 2])
// 选中的开始和结束时间
const selectedTimeStart = ref('')
const selectedTimeEnd = ref('')

// 时间段显示文本
const timeSlotText = computed(() => {
  if (selectedTimeStart.value && selectedTimeEnd.value) {
    return `${selectedTimeStart.value} - ${selectedTimeEnd.value}`
  }
  return ''
})

/**
 * 时间段列变化回调（联动：结束时间必须大于开始时间）
 */
function onTimeSlotColumnChange(e) {
  const { column, value } = e.detail
  const idx = [...timeSlotIndex.value]
  idx[column] = value
  // 如果修改了开始时间，确保结束时间大于开始时间
  if (column === 0) {
    // 结束时间至少比开始时间晚1小时（endHours 索引 >= startHours 索引 + 1）
    if (idx[1] <= idx[0]) {
      idx[1] = idx[0] + 1
      if (idx[1] >= endHours.length) {
        idx[1] = endHours.length - 1
      }
    }
  }
  timeSlotIndex.value = idx
}

/**
 * 时间段选择确认回调
 */
function onTimeSlotChange(e) {
  const [startIdx, endIdx] = e.detail.value
  selectedTimeStart.value = startHours[startIdx]
  selectedTimeEnd.value = endHours[endIdx]
  timeSlotIndex.value = [startIdx, endIdx]
}

// ==================== 衣物分类相关 ====================

// 可选的衣物分类
const categoryOptions = ['外套', '裤子', '衬衫', 'T恤', '裙子', '羽绒服', '毛衣', '鞋子', '包包', '其他']

// 已选中的分类
const selectedCategories = ref([])

/**
 * 切换分类选中状态
 */
function toggleCategory(item) {
  const index = selectedCategories.value.indexOf(item)
  if (index > -1) {
    selectedCategories.value.splice(index, 1)
  } else {
    selectedCategories.value.push(item)
  }
}

// ==================== 照片上传相关 ====================

// 已上传的照片URL列表
const photoList = ref([])
// 是否正在上传
const uploading = ref(false)

/**
 * 选择照片并上传
 * 使用 canvas 压缩图片至 ≤1MB 后再上传
 */
function choosePhoto() {
  const remaining = 6 - photoList.value.length
  if (remaining <= 0) return

  uni.chooseImage({
    count: remaining,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      uploading.value = true
      uni.showLoading({ title: '上传中...', mask: true })

      try {
        for (const tempPath of res.tempFilePaths) {
          // 压缩图片
          const compressedPath = await compressImage(tempPath)
          // 上传到服务器
          const url = await uploadFile(compressedPath)
          photoList.value.push(url)
        }
      } catch (e) {
        console.error('上传照片失败:', e)
        uni.showToast({ title: '上传失败，请重试', icon: 'none' })
      } finally {
        uploading.value = false
        uni.hideLoading()
      }
    }
  })
}

/**
 * 压缩图片（使用 canvas）
 * 将图片压缩至宽度不超过 1200px，质量 0.7，确保 ≤1MB
 * @param {string} src 原始图片临时路径
 * @returns {Promise<string>} 压缩后的临时路径
 */
function compressImage(src) {
  return new Promise((resolve, reject) => {
    uni.getImageInfo({
      src,
      success: (info) => {
        // 计算压缩后的尺寸，最大宽度 1200px
        const maxWidth = 1200
        let targetWidth = info.width
        let targetHeight = info.height

        if (targetWidth > maxWidth) {
          const ratio = maxWidth / targetWidth
          targetWidth = maxWidth
          targetHeight = Math.round(info.height * ratio)
        }

        // 创建离屏 canvas 进行压缩
        const canvas = uni.createOffscreenCanvas({
          type: '2d',
          width: targetWidth,
          height: targetHeight
        })
        const ctx = canvas.getContext('2d')

        // 加载图片到 canvas
        const image = canvas.createImage()
        image.onload = () => {
          ctx.drawImage(image, 0, 0, targetWidth, targetHeight)
          // 导出为临时文件（JPEG 质量 0.7）
          uni.canvasToTempFilePath({
            canvas,
            x: 0,
            y: 0,
            width: targetWidth,
            height: targetHeight,
            destWidth: targetWidth,
            destHeight: targetHeight,
            fileType: 'jpg',
            quality: 0.7,
            success: (res) => {
              resolve(res.tempFilePath)
            },
            fail: () => {
              // 压缩失败则使用原图
              resolve(src)
            }
          })
        }
        image.onerror = () => {
          // 加载失败则使用原图
          resolve(src)
        }
        image.src = src
      },
      fail: () => {
        // 获取图片信息失败则使用原图
        resolve(src)
      }
    })
  })
}

/**
 * 预览照片
 */
function previewPhoto(index) {
  // 照片URL可能是相对路径，需要拼接完整URL
  const urls = photoList.value.map(p => p.startsWith('http') ? p : BASE_URL + p)
  uni.previewImage({
    current: urls[index],
    urls
  })
}

/**
 * 删除照片
 */
function removePhoto(index) {
  photoList.value.splice(index, 1)
}

// ==================== 提交订单 ====================

/**
 * 表单校验
 */
function validate() {
  if (!selectedAddress.value) {
    uni.showToast({ title: '请选择上门地址', icon: 'none' })
    return false
  }
  if (!form.value.appointmentDate) {
    uni.showToast({ title: '请选择预约日期', icon: 'none' })
    return false
  }
  if (!selectedTimeStart.value || !selectedTimeEnd.value) {
    uni.showToast({ title: '请选择预约时间段', icon: 'none' })
    return false
  }
  if (selectedCategories.value.length === 0) {
    uni.showToast({ title: '请选择衣物分类', icon: 'none' })
    return false
  }
  if (!form.value.estimatedWeight || parseFloat(form.value.estimatedWeight) <= 0) {
    uni.showToast({ title: '请输入有效的预估重量', icon: 'none' })
    return false
  }
  return true
}

/**
 * 提交预约订单
 */
async function handleSubmit() {
  if (!validate()) return

  try {
    const data = {
      addressId: selectedAddress.value.id,
      appointmentDate: form.value.appointmentDate,
      timeSlotStart: selectedTimeStart.value,
      timeSlotEnd: selectedTimeEnd.value,
      estimatedWeight: parseFloat(form.value.estimatedWeight),
      clothesCategories: selectedCategories.value,
      photos: photoList.value.length > 0 ? photoList.value : null,
      remark: form.value.remark || null
    }

    await request({
      url: '/api/user/order/create',
      method: 'POST',
      data
    })

    uni.showToast({ title: '预约成功', icon: 'success' })
    // 延迟跳转到订单列表
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/order/list/list' })
    }, 1000)
  } catch (e) {
    console.error('提交订单失败:', e)
  }
}
</script>

<style scoped>
/* 页面容器 */
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx 24rpx;
  padding-bottom: 260rpx;
}

/* 区块样式 */
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 地址卡片 */
.address-card {
  position: relative;
  padding: 16rpx 40rpx 16rpx 0;
}

.address-info {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.address-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-right: 16rpx;
}

.address-phone {
  font-size: 26rpx;
  color: #666;
}

.address-detail-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.address-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
}

.empty-hint {
  font-size: 28rpx;
  color: #999;
}

.arrow {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  color: #ccc;
  font-size: 28rpx;
}

.address-empty .arrow {
  position: static;
  transform: none;
}

/* 表单项 */
.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.column {
  flex-direction: column;
  align-items: flex-start;
}

.label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
}

.form-item.column .label {
  width: auto;
  margin-bottom: 16rpx;
}

.value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  text-align: right;
}

.value.placeholder {
  color: #c0c0c0;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  text-align: right;
}

.unit {
  font-size: 26rpx;
  color: #999;
  margin-left: 8rpx;
}

/* 衣物分类标签 */
.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  width: 100%;
}

.category-tag {
  padding: 12rpx 28rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
  background: #f5f5f5;
  border: 1rpx solid #e0e0e0;
}

.category-tag.active {
  color: #07c160;
  background: #e8f8ee;
  border-color: #07c160;
}

/* 照片上传 */
.photo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.photo-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.photo-img {
  width: 100%;
  height: 100%;
}

.photo-delete {
  position: absolute;
  top: 0;
  right: 0;
  width: 44rpx;
  height: 44rpx;
  line-height: 44rpx;
  text-align: center;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 32rpx;
  border-radius: 0 0 0 12rpx;
}

.photo-add {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  border: 2rpx dashed #ccc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.photo-add-icon {
  font-size: 60rpx;
  color: #ccc;
  line-height: 1;
}

.photo-add-text {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 备注输入框 */
.remark-input {
  width: 100%;
  min-height: 120rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

/* 底部提交按钮 */
.bottom-bar {
  position: fixed;
  bottom: 120rpx;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
}

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
}
</style>
