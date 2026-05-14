<template>
  <div class="receive-page">
    <!-- 接收区域 -->
    <el-card shadow="hover" class="receive-card">
      <template #header><span class="card-title">扫码 / 手动接收订单</span></template>
      <div class="receive-form">
        <el-input
          v-model="orderNo"
          placeholder="请输入或扫描订单编号"
          clearable
          size="large"
          style="width: 400px;"
          @keyup.enter="handleReceive"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" :loading="receiving" @click="handleReceive">
          确认接收
        </el-button>
      </div>
      <p class="receive-tip">输入回收员提供的订单编号（如 RC202604...），确认接收后将自动发放积分给用户</p>
    </el-card>

    <!-- 接收结果 -->
    <el-card v-if="receiveResult" shadow="hover" class="result-card">
      <el-result
        :icon="receiveResult.success ? 'success' : 'error'"
        :title="receiveResult.success ? '接收成功' : '接收失败'"
        :sub-title="receiveResult.message"
      >
        <template #extra>
          <el-button type="primary" @click="handleContinue">继续接收</el-button>
        </template>
      </el-result>
    </el-card>

    <!-- 待分配去向列表 -->
    <el-card shadow="hover" class="unassigned-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">待分配去向的订单</span>
          <el-button type="primary" text @click="loadUnassigned">刷新</el-button>
        </div>
      </template>
      <el-table :data="unassignedOrders" v-loading="unassignedLoading" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="200" />
        <el-table-column prop="clothesCategories" label="衣物分类" min-width="140" />
        <el-table-column label="实际重量" min-width="100">
          <template #default="{ row }">{{ row.actualWeight ? row.actualWeight + ' kg' : '-' }}</template>
        </el-table-column>
        <el-table-column label="积分" min-width="80">
          <template #default="{ row }">{{ row.pointsAwarded ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="openAssign(row)">分配去向</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { scanReceiveOrder, getUnassignedOrders, assignDestination } from '../../api/institution'

/* ========== 扫码接收 ========== */

const orderNo = ref('')
const receiving = ref(false)
const receiveResult = ref(null)

async function handleReceive() {
  const no = orderNo.value.trim()
  if (!no) {
    ElMessage.warning('请输入订单编号')
    return
  }
  receiving.value = true
  receiveResult.value = null
  try {
    await scanReceiveOrder(no)
    receiveResult.value = { success: true, message: `订单 ${no} 接收成功，积分已发放给用户` }
    orderNo.value = ''
    loadUnassigned()
  } catch (e) {
    receiveResult.value = { success: false, message: e.response?.data?.message || e.message || '接收失败' }
  } finally {
    receiving.value = false
  }
}

function handleContinue() {
  receiveResult.value = null
  orderNo.value = ''
}

/* ========== 待分配去向列表 ========== */

const unassignedOrders = ref([])
const unassignedLoading = ref(false)

async function loadUnassigned() {
  unassignedLoading.value = true
  try {
    unassignedOrders.value = await getUnassignedOrders() || []
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    unassignedLoading.value = false
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
    loadUnassigned()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    assignLoading.value = false
  }
}

/* ========== 工具函数 ========== */

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadUnassigned()
})
</script>

<style scoped>
.receive-page {
  padding: 20px;
}

.receive-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.receive-form {
  display: flex;
  gap: 16px;
  align-items: center;
}

.receive-tip {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
}

.result-card {
  margin-bottom: 20px;
}

.unassigned-card {
  margin-bottom: 20px;
}
</style>
