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
      <el-button type="success" @click="handleExport">导出 Excel</el-button>
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
          <el-button type="primary" link @click="orderDetailRef.open(row.id)">查看详情</el-button>
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
    <OrderDetailDialog ref="orderDetailRef" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders } from '../../api/order'
import OrderDetailDialog from '../../components/OrderDetailDialog.vue'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

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
const orderDetailRef = ref(null)

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

/* ========== 导出 Excel ========== */

function handleExport() {
  if (!orders.value.length) {
    ElMessage.warning('当前无数据可导出')
    return
  }
  const rows = orders.value.map(row => ({
    '订单号': row.orderNo,
    '下单用户': `${row.userName || '-'}（${row.userPhone || '-'}）`,
    '衣物分类': row.clothesCategories || '-',
    '预估重量(kg)': row.estimatedWeight ?? '-',
    '实际重量(kg)': row.actualWeight ?? '-',
    '状态': statusText(row.status),
    '积分': row.pointsAwarded ?? '-',
    '下单时间': formatTime(row.createdAt)
  }))
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '订单列表')
  const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([buf], { type: 'application/octet-stream' }), '订单列表.xlsx')
}

/* ========== 工具函数 ========== */

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
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
</style>
