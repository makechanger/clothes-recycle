<template>
  <!-- 回收员管理页面：包含回收员列表和待审核申请两个 Tab -->
  <div class="collector-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- Tab 1：回收员列表 -->
      <el-tab-pane label="回收员列表" name="list">
        <!-- 顶部操作栏 -->
        <div class="toolbar">
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            创建回收员
          </el-button>
        </div>

        <!-- 回收员表格 -->
        <el-table :data="collectors" v-loading="loadingCollectors" stripe>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column label="资质状态" min-width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.collectorStatus)">
                {{ statusText(row.collectorStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="身份证照片" min-width="120">
            <template #default="{ row }">
              <el-image
                v-if="row.idCardPhoto"
                :src="row.idCardPhoto"
                :preview-src-list="[row.idCardPhoto]"
                fit="cover"
                style="width: 60px; height: 40px; cursor: pointer;"
                preview-teleported
              />
              <span v-else style="color: #999;">未上传</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab 2：待审核申请 -->
      <el-tab-pane label="待审核申请" name="pending">
        <el-table :data="pendingApplications" v-loading="loadingApplications" stripe>
          <el-table-column label="申请角色" min-width="120">
            <template #default="{ row }">
              <el-tag>{{ row.applyRole === 'COLLECTOR' ? '回收员' : '机构' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名/机构名" min-width="140" />
          <el-table-column label="身份证照片" min-width="120">
            <template #default="{ row }">
              <el-image
                v-if="row.idCardPhoto"
                :src="row.idCardPhoto"
                :preview-src-list="[row.idCardPhoto]"
                fit="cover"
                style="width: 60px; height: 40px; cursor: pointer;"
                preview-teleported
              />
              <span v-else style="color: #999;">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="180">
            <template #default="{ row }">
              {{ row.address || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="contactPerson" label="联系人" min-width="120">
            <template #default="{ row }">
              {{ row.contactPerson || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" min-width="180">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
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
          <el-table-column label="申请角色" min-width="100">
            <template #default="{ row }">
              <el-tag>{{ row.applyRole === 'COLLECTOR' ? '回收员' : '机构' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名/机构名" min-width="140" />
          <el-table-column label="审批结果" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="拒绝原因" min-width="180">
            <template #default="{ row }">
              {{ row.rejectReason || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="申请时间" min-width="170">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="处理时间" min-width="170">
            <template #default="{ row }">
              {{ formatTime(row.updatedAt) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建回收员弹窗 -->
    <el-dialog v-model="showCreateDialog" title="创建回收员" width="450px" @close="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入回收员姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确认创建</el-button>
      </template>
    </el-dialog>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCollectors,
  createCollector,
  getPendingApplications,
  getProcessedApplications,
  approveApplication,
  rejectApplication
} from '../../api/collector'

/* ========== 状态变量 ========== */

const activeTab = ref('list')

// 回收员列表
const collectors = ref([])
const loadingCollectors = ref(false)

// 待审核申请
const pendingApplications = ref([])
const loadingApplications = ref(false)

// 审批记录
const processedApplications = ref([])
const loadingProcessed = ref(false)

// 创建回收员弹窗
const showCreateDialog = ref(false)
const createFormRef = ref(null)
const creating = ref(false)
const createForm = reactive({
  phone: '',
  password: '',
  name: ''
})
const createRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ]
}

// 拒绝弹窗
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
const currentRejectId = ref(null)

/* ========== 数据加载 ========== */

/** 加载回收员列表 */
async function loadCollectors() {
  loadingCollectors.value = true
  try {
    collectors.value = await getCollectors()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loadingCollectors.value = false
  }
}

/** 加载待审核申请列表 */
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

/** 加载已处理的审批记录 */
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

/** Tab 切换时加载对应数据 */
function handleTabChange(tab) {
  if (tab === 'list') {
    loadCollectors()
  } else if (tab === 'pending') {
    loadPendingApplications()
  } else if (tab === 'history') {
    loadProcessedApplications()
  }
}

/* ========== 创建回收员 ========== */

/** 提交创建回收员 */
async function handleCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    await createCollector({
      phone: createForm.phone,
      password: createForm.password,
      name: createForm.name
    })
    ElMessage.success('回收员创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadCollectors()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    creating.value = false
  }
}

/** 重置创建表单 */
function resetCreateForm() {
  createForm.phone = ''
  createForm.password = ''
  createForm.name = ''
}

/* ========== 审批操作 ========== */

/** 审批通过 */
async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确认通过「${row.name}」的${row.applyRole === 'COLLECTOR' ? '回收员' : '机构'}申请？`,
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
    loadCollectors()
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

/** 点击拒绝按钮，弹出原因输入框 */
function handleRejectClick(row) {
  currentRejectId.value = row.id
  rejectReason.value = ''
  showRejectDialog.value = true
}

/** 确认拒绝 */
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

/* ========== 工具函数 ========== */

/** 资质状态文字 */
function statusText(status) {
  const map = { 0: '待完善资质', 1: '待审核', 2: '已认证', 3: '已禁用' }
  return map[status] || '未知'
}

/** 资质状态标签颜色 */
function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

/** 格式化时间 */
function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

/* ========== 初始化 ========== */

onMounted(() => {
  loadCollectors()
})
</script>

<style scoped>
.collector-page {
  padding: 0;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
