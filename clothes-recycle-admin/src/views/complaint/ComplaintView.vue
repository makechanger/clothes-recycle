<template>
  <div class="complaint-page">
    <h2>申诉管理</h2>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="handleFilter">
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
      </el-select>
    </div>

    <!-- 申诉列表 -->
    <el-table :data="complaints" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="订单号" width="180">
        <template #default="{ row }">
          <el-button type="primary" link @click="orderDetailRef.open(row.orderId)">{{ row.orderNo }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column label="申诉类型" width="120">
        <template #default="{ row }">
          {{ typeMap[row.type] || row.type }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : 'success'">
            {{ row.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminRemark" label="处理备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="处理动作" width="140">
        <template #default="{ row }">
          <template v-if="row.action === 'ADD_POINTS'">
            <el-tag type="success">增加积分({{ row.refundAmount }})</el-tag>
          </template>
          <template v-else-if="row.action === 'DEDUCT_POINTS'">
            <el-tag type="warning">减少积分({{ row.refundAmount }})</el-tag>
          </template>
          <template v-else-if="row.action === 'MARK_ABNORMAL'">
            <el-tag type="danger">标记异常</el-tag>
          </template>
          <template v-else>
            {{ row.action || '-' }}
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="170">
        <template #default="{ row }">
          {{ row.createdAt ? row.createdAt.replace('T', ' ') : '-' }}
        </template>
      </el-table-column>

      <el-table-column prop="handledAt" label="处理时间" width="170">
        <template #default="{ row }">
          {{ row.handledAt ? row.handledAt.replace('T', ' ') : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            @click="openHandleDialog(row)"
          >处理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 订单详情弹窗 -->
    <OrderDetailDialog ref="orderDetailRef" />

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadComplaints"
      />
    </div>

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理申诉" width="500px">
      <el-form label-width="100px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleAction">
            <el-radio value="ADD_POINTS">增加积分</el-radio>
            <el-radio value="DEDUCT_POINTS">减少积分</el-radio>
            <el-radio value="MARK_ABNORMAL">标记订单异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="handleAction === 'ADD_POINTS' || handleAction === 'DEDUCT_POINTS'" label="积分数量">
          <el-input-number v-model="refundAmount" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="adminRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getComplaints, handleComplaint } from '../../api/complaint'
import OrderDetailDialog from '../../components/OrderDetailDialog.vue'

const orderDetailRef = ref(null)

const typeMap = {
  WEIGHT_DISPUTE: '称重不符',
  SERVICE_ISSUE: '服务问题',
  ITEM_LOST: '物品丢失',
  OTHER: '其他'
}

const complaints = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filterStatus = ref(null)

const dialogVisible = ref(false)
const adminRemark = ref('')
const handleAction = ref('MARK_HANDLED')
const refundAmount = ref(1)
const currentId = ref(null)
const submitting = ref(false)

async function loadComplaints() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value !== null && filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    const res = await getComplaints(params)
    complaints.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  page.value = 1
  loadComplaints()
}

function openHandleDialog(row) {
  currentId.value = row.id
  adminRemark.value = ''
  handleAction.value = 'ADD_POINTS'
  refundAmount.value = 1
  dialogVisible.value = true
}

async function confirmHandle() {
  const needsAmount = handleAction.value === 'ADD_POINTS' || handleAction.value === 'DEDUCT_POINTS'
  if (needsAmount && (!refundAmount.value || refundAmount.value <= 0)) {
    ElMessage.warning('请输入积分数量')
    return
  }
  submitting.value = true
  try {
    const data = {
      adminRemark: adminRemark.value,
      action: handleAction.value
    }
    if (handleAction.value === 'ADD_POINTS' || handleAction.value === 'DEDUCT_POINTS') {
      data.refundAmount = refundAmount.value
    }
    await handleComplaint(currentId.value, data)
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadComplaints()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '处理失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadComplaints())
</script>

<style scoped>
.complaint-page {
  padding: 20px;
}
h2 {
  margin-bottom: 20px;
  color: #303133;
}
.filter-bar {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
