<template>
  <div class="order-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filters.status" placeholder="订单状态" clearable style="width: 160px;">
        <el-option label="机构已接收" :value="7" />
        <el-option label="已分配去向" :value="8" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 订单表格 -->
    <el-table :data="pagedOrders" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="200" />
      <el-table-column prop="clothesCategories" label="衣物分类" min-width="140" />
      <el-table-column label="实际重量" min-width="100">
        <template #default="{ row }">
          {{ row.actualWeight ? row.actualWeight + ' kg' : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="积分" min-width="80">
        <template #default="{ row }">{{ row.pointsAwarded ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 8 ? 'success' : 'warning'">
            {{ row.status === 8 ? '已分配去向' : '待分配去向' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="去向" min-width="140">
        <template #default="{ row }">
          <span v-if="row.destinationType">{{ destLabel(row.destinationType) }}</span>
          <span v-else style="color: #999;">未分配</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="170">
        <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row.id)">详情</el-button>
          <el-button v-if="row.status === 7" type="success" link @click="openAssign(row)">分配去向</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="filteredOrders.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="620px" :destroy-on-close="true">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ detailData.order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="衣物分类">{{ detailData.order.clothesCategories || '-' }}</el-descriptions-item>
            <el-descriptions-item label="实际重量">{{ detailData.order.actualWeight ? detailData.order.actualWeight + ' kg' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="获得积分">{{ detailData.order.pointsAwarded ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.order.status === 8 ? 'success' : 'warning'">
                {{ detailData.order.status === 8 ? '已分配去向' : '待分配去向' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="地址">{{ formatAddress(detailData.order.addressSnapshot) }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatTime(detailData.order.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatTime(detailData.order.completedAt) }}</el-descriptions-item>
          </el-descriptions>

          <!-- 溯源二维码 -->
          <div v-if="detailData.order.qrCode" class="section">
            <h4>溯源二维码</h4>
            <el-image
              class="qr-image"
              :src="detailData.order.qrCode"
              :preview-src-list="[detailData.order.qrCode]"
              fit="contain"
              preview-teleported
            />
          </div>

          <!-- 去向信息 -->
          <div v-if="detailData.destination" class="section">
            <h4>衣物去向</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="去向类型">{{ detailData.destination.destinationType }}</el-descriptions-item>
              <el-descriptions-item label="去向说明">{{ detailData.destination.destinationDesc || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 状态日志 -->
          <div v-if="detailData.statusLogs && detailData.statusLogs.length" class="section">
            <h4>状态流转</h4>
            <el-timeline>
              <el-timeline-item
                v-for="log in detailData.statusLogs"
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

    <!-- 分配去向弹窗 -->
    <el-dialog v-model="assignVisible" title="分配衣物去向" width="500px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="订单号">
          <span>{{ assignForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="去向类型" required>
          <el-select v-model="assignForm.destinationType" placeholder="请选择去向类型" style="width: 100%;">
            <el-option label="捐赠" value="DONATION" />
            <el-option label="再生利用" value="RECYCLE" />
            <el-option label="环保处理" value="ENVIRONMENTAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="去向说明">
          <el-input v-model="assignForm.description" type="textarea" :rows="3" placeholder="请输入去向说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="handleAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInstitutionOrders, getInstitutionOrderDetail, assignDestination } from '../../api/institution'

const loading = ref(false)
const allOrders = ref([])
const filters = reactive({ status: null })
const pagination = reactive({ page: 1, size: 10 })

const filteredOrders = computed(() => {
  if (filters.status !== null && filters.status !== '') {
    return allOrders.value.filter(o => o.status === filters.status)
  }
  return allOrders.value
})

const pagedOrders = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredOrders.value.slice(start, start + pagination.size)
})

async function loadOrders() {
  loading.value = true
  try {
    allOrders.value = await getInstitutionOrders({}) || []
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  filters.status = null
  pagination.page = 1
}

/* ========== 详情弹窗 ========== */

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

async function openDetail(orderId) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await getInstitutionOrderDetail(orderId)
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    detailLoading.value = false
  }
}

/* ========== 分配去向 ========== */

const assignVisible = ref(false)
const assignLoading = ref(false)
const assignForm = reactive({ orderId: null, orderNo: '', destinationType: '', description: '' })

function openAssign(row) {
  assignForm.orderId = row.id
  assignForm.orderNo = row.orderNo
  assignForm.destinationType = ''
  assignForm.description = ''
  assignVisible.value = true
}

async function handleAssign() {
  if (!assignForm.destinationType) {
    ElMessage.warning('请选择去向类型')
    return
  }
  assignLoading.value = true
  try {
    await assignDestination({
      orderId: assignForm.orderId,
      destinationType: assignForm.destinationType,
      description: assignForm.description
    })
    ElMessage.success('分配成功')
    assignVisible.value = false
    loadOrders()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    assignLoading.value = false
  }
}

/* ========== 工具函数 ========== */

function destLabel(type) {
  const map = { DONATION: '捐赠', RECYCLE: '再生利用', ENVIRONMENTAL: '环保处理' }
  return map[type] || type
}

function statusText(status) {
  const map = { 0: '待接单', 1: '已接单', 2: '上门中', 3: '待确认', 4: '已完成', 5: '已取消', 6: '异常', 7: '机构已接收', 8: '已分配去向' }
  return map[status] ?? '未知'
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function formatAddress(addressSnapshot) {
  if (!addressSnapshot) return '-'
  try {
    const addr = typeof addressSnapshot === 'string' ? JSON.parse(addressSnapshot) : addressSnapshot
    return [addr.province, addr.city, addr.district, addr.detailAddress].filter(Boolean).join('') || '-'
  } catch {
    return addressSnapshot
  }
}

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
.qr-image {
  width: 180px;
  height: 180px;
  border-radius: 6px;
  background: #fff;
}
</style>
