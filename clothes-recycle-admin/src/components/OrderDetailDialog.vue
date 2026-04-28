<template>
  <el-dialog
    v-model="visible"
    title="订单详情"
    width="620px"
    :destroy-on-close="true"
  >
    <div v-loading="loading">
      <template v-if="detail">
        <!-- 基本信息 -->
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单用户">
            {{ detail.userName || '-' }}（{{ detail.userPhone || '-' }}）
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="衣物分类">{{ detail.clothesCategories || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预估重量">{{ detail.estimatedWeight ? detail.estimatedWeight + ' kg' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际重量">{{ detail.actualWeight ? detail.actualWeight + ' kg' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="获得积分">{{ detail.pointsAwarded ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="预约日期">{{ detail.appointmentDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间段">
            {{ detail.timeSlotStart && detail.timeSlotEnd ? detail.timeSlotStart + ' ~ ' + detail.timeSlotEnd : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="地址">{{ formatAddress(detail.addressSnapshot) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="接单时间">{{ formatTime(detail.acceptedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatTime(detail.completedAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 衣物照片 -->
        <div v-if="detail.photos" class="section">
          <h4>衣物照片</h4>
          <div class="photo-list">
            <el-image
              v-for="(url, idx) in parsePhotos(detail.photos)"
              :key="idx"
              :src="url"
              :preview-src-list="parsePhotos(detail.photos)"
              fit="cover"
              style="width: 80px; height: 80px; margin-right: 8px; border-radius: 4px;"
              preview-teleported
            />
          </div>
        </div>

        <!-- 状态流转日志 -->
        <div v-if="detail.statusLogs && detail.statusLogs.length" class="section">
          <h4>状态流转</h4>
          <el-timeline>
            <el-timeline-item
              v-for="log in detail.statusLogs"
              :key="log.id"
              :timestamp="formatTime(log.createdAt)"
              placement="top"
            >
              {{ statusText(log.fromStatus) }} → {{ statusText(log.toStatus) }}
              <span v-if="log.remark" style="color: #999; margin-left: 8px;">{{ log.remark }}</span>
            </el-timeline-item>
          </el-timeline>
        </div>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { getOrderDetail } from '../api/order'

const visible = ref(false)
const loading = ref(false)
const detail = ref(null)

function statusText(status) {
  const map = { 0: '待接单', 1: '已接单', 2: '上门中', 3: '待确认', 4: '已完成', 5: '已取消', 6: '异常', 7: '机构已接收', 8: '已分配去向' }
  return map[status] ?? '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'warning', 4: 'success', 5: 'info', 6: 'danger', 7: 'success', 8: 'success' }
  return map[status] ?? 'info'
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function formatAddress(addressSnapshot) {
  if (!addressSnapshot) return '-'
  try {
    const addr = typeof addressSnapshot === 'string' ? JSON.parse(addressSnapshot) : addressSnapshot
    const parts = [addr.province, addr.city, addr.district, addr.detailAddress].filter(Boolean)
    return parts.join('') || '-'
  } catch {
    return addressSnapshot
  }
}

function parsePhotos(photos) {
  if (!photos) return []
  if (Array.isArray(photos)) return photos
  try {
    return JSON.parse(photos)
  } catch {
    return photos.split(',').map(s => s.trim()).filter(Boolean)
  }
}

async function open(orderId) {
  visible.value = true
  loading.value = true
  detail.value = null
  try {
    detail.value = await getOrderDetail(orderId)
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.section {
  margin-top: 20px;
}
.section h4 {
  margin-bottom: 12px;
  color: #303133;
}
.photo-list {
  display: flex;
  flex-wrap: wrap;
}
</style>
