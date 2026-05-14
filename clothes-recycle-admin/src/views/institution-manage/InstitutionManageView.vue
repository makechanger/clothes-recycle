<template>
  <div class="institution-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- Tab 1：机构列表 -->
      <el-tab-pane label="机构列表" name="list">
        <el-table :data="institutions" v-loading="loadingInstitutions" stripe>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="name" label="机构名称" min-width="160" />
          <el-table-column prop="contactPerson" label="联系人" min-width="120" />
          <el-table-column prop="address" label="地址" min-width="200" />
          <el-table-column prop="type" label="类型" min-width="120" />
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab 2：待审核申请 -->
      <el-tab-pane label="待审核申请" name="pending">
        <el-table :data="pendingApplications" v-loading="loadingApplications" stripe>
          <el-table-column prop="name" label="机构名称" min-width="160" />
          <el-table-column prop="address" label="地址" min-width="200" />
          <el-table-column prop="contactPerson" label="联系人" min-width="120" />
          <el-table-column label="申请时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" min-width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleRejectClick(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab 3：审批记录 -->
      <el-tab-pane label="审批记录" name="history">
        <el-table :data="processedApplications" v-loading="loadingProcessed" stripe>
          <el-table-column prop="name" label="机构名称" min-width="160" />
          <el-table-column prop="address" label="地址" min-width="180" />
          <el-table-column prop="contactPerson" label="联系人" min-width="120" />
          <el-table-column label="审批结果" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="拒绝原因" min-width="180">
            <template #default="{ row }">{{ row.rejectReason || '-' }}</template>
          </el-table-column>
          <el-table-column label="申请时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="处理时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="showRejectDialog" title="拒绝申请" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入拒绝原因（选填）"
      />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getInstitutions,
  getPendingApplications,
  getProcessedApplications,
  approveApplication,
  rejectApplication
} from '../../api/institution-admin'

const activeTab = ref('list')

// 机构列表
const institutions = ref([])
const loadingInstitutions = ref(false)

// 待审核申请
const pendingApplications = ref([])
const loadingApplications = ref(false)

// 审批记录
const processedApplications = ref([])
const loadingProcessed = ref(false)

// 拒绝弹窗
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
const currentRejectId = ref(null)

async function loadInstitutions() {
  loadingInstitutions.value = true
  try {
    institutions.value = await getInstitutions()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loadingInstitutions.value = false
  }
}

async function loadPendingApplications() {
  loadingApplications.value = true
  try {
    pendingApplications.value = await getPendingApplications()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loadingApplications.value = false
  }
}

async function loadProcessedApplications() {
  loadingProcessed.value = true
  try {
    processedApplications.value = await getProcessedApplications()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loadingProcessed.value = false
  }
}

function handleTabChange(tab) {
  if (tab === 'list') {
    loadInstitutions()
  } else if (tab === 'pending') {
    loadPendingApplications()
  } else if (tab === 'history') {
    loadProcessedApplications()
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确认通过「${row.name}」的机构申请？`,
      '审批确认',
      { confirmButtonText: '通过', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  try {
    await approveApplication(row.id)
    ElMessage.success('审批通过')
    loadPendingApplications()
    loadInstitutions()
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

function handleRejectClick(row) {
  currentRejectId.value = row.id
  rejectReason.value = ''
  showRejectDialog.value = true
}

async function handleReject() {
  rejecting.value = true
  try {
    await rejectApplication(currentRejectId.value, rejectReason.value)
    ElMessage.success('已拒绝该申请')
    showRejectDialog.value = false
    loadPendingApplications()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    rejecting.value = false
  }
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadInstitutions()
})
</script>

<style scoped>
.institution-page {
  padding: 0;
}
</style>
