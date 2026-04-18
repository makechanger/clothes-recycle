<template>
  <!-- 订单管理页面：筛选 + 分页表格 + 详情弹窗 -->
  <div class="order-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.orderNo"
        placeholder="搜索订单号"
        clearable
        style="width: 220px;"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="filters.status"
        placeholder="订单状态"
        clearable
        style="width: 160px;"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 订单表格 -->
    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column label="下单用户" min-width="140">
        <template #default="{ row }">
          {{ row.userName || '-' }}（{{ row.userPhone || '-' }}）
        </template>
      </el-table-column>
      <el-table-column prop="clothesCategories" label="衣物分类" min-width="140" />
      <el-table-column label="预估重量" min-width="100">
        <template #default="{ row }">
          {{ row.estimatedWeight ? row.estimatedWeight + ' kg' : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="实际重量" min-width="100">
        <template #default="{ row }">
          {{ row.actualWeight ? row.actualWeight + ' kg' : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分" min-width="80">
        <template #default="{ row }">
          {{ row.pointsAwarded ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="下单时间" min-width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleViewDetail(row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="showDetail"
      title="订单详情"
      width="620px"
      :destroy-on-close="true"
    >
      <div v-loading="loadingDetail">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOrders, getOrderDetail } from '../../api/order'

/* ========== 订单状态映射 ========== */

const statusOptions = [
  { value: 0, label: '待接单' },
  { value: 1, label: '已接单' },
  { value: 2, label: '上门中' },
  { value: 3, label: '待确认' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已取消' },
  { value: 6, label: '异常' },
  { value: 7, label: '机构已接收' }
]

function statusText(status) {
  const map = { 0: '待接单', 1: '已接单', 2: '上门中', 3: '待确认', 4: '已完成', 5: '已取消', 6: '异常', 7: '机构已接收' }
  return map[status] ?? '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'warning', 4: 'success', 5: 'info', 6: 'danger', 7: 'success' }
  return map[status] ?? 'info'
}

/* ========== 状态变量 ========== */

const loading = ref(false)
const orders = ref([])
const filters = reactive({ orderNo: '', status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const showDetail = ref(false)
const loadingDetail = ref(false)
const detail = ref(null)

/* ========== 数据加载 ========== */

async function loadOrders() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (filters.status !== null && filters.status !== '') {
      params.status = filters.status
    }
    if (filters.orderNo) {
      params.orderNo = filters.orderNo
    }
    const data = await getOrders(params)
    orders.value = data.records || []
    pagination.total = data.total || 0
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadOrders()
}

function handleReset() {
  filters.orderNo = ''
  filters.status = null
  pagination.page = 1
  loadOrders()
}

async function handleViewDetail(id) {
  showDetail.value = true
  loadingDetail.value = true
  detail.value = null
  try {
    detail.value = await getOrderDetail(id)
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loadingDetail.value = false
  }
}

/* ========== 工具函数 ========== */

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

/** 将地址快照 JSON 解析为可读字符串（省+市+区+详细地址） */
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

/* ========== 初始化 ========== */

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

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
