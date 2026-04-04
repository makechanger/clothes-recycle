<template>
  <!-- 地址编辑页：新增或编辑用户地址，支持省市区选择器 -->
  <view class="page">
    <view class="form">
      <!-- 联系人姓名 -->
      <view class="form-item">
        <text class="label">联系人</text>
        <input v-model="form.name" class="input" placeholder="请输入联系人姓名" />
      </view>

      <!-- 联系电话 -->
      <view class="form-item">
        <text class="label">联系电话</text>
        <input v-model="form.phone" class="input" type="number" placeholder="请输入手机号" maxlength="11" />
      </view>

      <!-- 省市区选择器（picker 包裹整个表单项，点击即弹出） -->
      <picker mode="region" :value="regionValue" @change="onRegionChange">
        <view class="form-item">
          <text class="label">所在地区</text>
          <text :class="['input', 'picker-text', { placeholder: !regionText }]">
            {{ regionText || '请选择省/市/区' }}
          </text>
          <text class="arrow">></text>
        </view>
      </picker>

      <!-- 详细地址 -->
      <view class="form-item">
        <text class="label">详细地址</text>
        <input v-model="form.detailAddress" class="input" placeholder="请输入详细地址（楼栋/门牌号等）" />
      </view>

      <!-- 设为默认地址 -->
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch :checked="form.isDefault === 1" color="#07c160" @change="onDefaultChange" />
      </view>
    </view>

    <!-- 保存按钮 -->
    <button class="save-btn" @click="handleSave">保存</button>
  </view>
</template>

<script setup>
/**
 * 地址编辑页
 * - 无 id 参数时为新增模式，有 id 参数时为编辑模式
 * - 省市区使用 uni-app 内置 picker（mode="region"）
 * - 保存成功后返回地址列表页
 */
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

// 地址ID（编辑模式下有值）
const addressId = ref(null)

// 表单数据
const form = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

// 省市区选择器的值（数组格式）
const regionValue = ref([])

// 省市区显示文本
const regionText = computed(() => {
  if (form.value.province) {
    return `${form.value.province} ${form.value.city} ${form.value.district}`
  }
  return ''
})

// 页面加载时判断新增还是编辑
onLoad((options) => {
  if (options.id) {
    addressId.value = options.id
    // 设置导航栏标题为"编辑地址"
    uni.setNavigationBarTitle({ title: '编辑地址' })
    loadAddress(options.id)
  } else {
    uni.setNavigationBarTitle({ title: '新增地址' })
  }
})

/**
 * 加载地址详情（编辑模式）
 */
async function loadAddress(id) {
  try {
    const data = await request({ url: `/api/user/address/${id}` })
    form.value = {
      name: data.name,
      phone: data.phone,
      province: data.province,
      city: data.city,
      district: data.district,
      detailAddress: data.detailAddress,
      isDefault: data.isDefault
    }
    regionValue.value = [data.province, data.city, data.district]
  } catch (e) {
    console.error('加载地址详情失败:', e)
  }
}

/**
 * 省市区选择完成回调
 */
function onRegionChange(e) {
  const [province, city, district] = e.detail.value
  form.value.province = province
  form.value.city = city
  form.value.district = district
  regionValue.value = [province, city, district]
}

/**
 * 默认地址开关变更
 */
function onDefaultChange(e) {
  form.value.isDefault = e.detail.value ? 1 : 0
}

/**
 * 表单校验
 */
function validate() {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
    return false
  }
  if (!form.value.phone || !/^1\d{10}$/.test(form.value.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  if (!form.value.province) {
    uni.showToast({ title: '请选择所在地区', icon: 'none' })
    return false
  }
  if (!form.value.detailAddress.trim()) {
    uni.showToast({ title: '请输入详细地址', icon: 'none' })
    return false
  }
  return true
}

/**
 * 保存地址（新增或编辑）
 */
async function handleSave() {
  if (!validate()) return

  try {
    const url = addressId.value
      ? `/api/user/address/${addressId.value}/update`
      : '/api/user/address/create'

    await request({
      url,
      method: 'POST',
      data: form.value
    })

    uni.showToast({ title: '保存成功', icon: 'success' })
    // 延迟返回，让用户看到提示
    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch (e) {
    console.error('保存地址失败:', e)
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx 24rpx;
}

.form {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.picker-text {
  line-height: normal;
}

.picker-text.placeholder {
  color: #c0c0c0;
}

.arrow {
  color: #ccc;
  font-size: 28rpx;
  margin-left: 10rpx;
}

/* 默认地址开关 */
.switch-item {
  justify-content: space-between;
}

/* 保存按钮 */
.save-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  text-align: center;
  border: none;
  margin-top: 40rpx;
}
</style>
