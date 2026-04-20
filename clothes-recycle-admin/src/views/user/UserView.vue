<template>
  <!-- 用户管理页面：筛选 + 分页表格 + 修改角色弹窗 -->
  <div class="user-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="手机号/姓名"
        clearable
        style="width: 200px;"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="filters.role"
        placeholder="角色"
        clearable
        style="width: 140px;"
      >
        <el-option label="普通用户" value="USER" />
        <el-option label="回收员" value="COLLECTOR" />
        <el-option label="机构" value="INSTITUTION" />
      </el-select>
      <el-select
        v-model="filters.status"
        placeholder="状态"
        clearable
        style="width: 120px;"
      >
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleExport">导出 Excel</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column label="姓名" min-width="120">
        <template #default="{ row }">
          {{ row.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="120">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分余额" min-width="100">
        <template #default="{ row }">
          {{ row.pointsBalance ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" min-width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="260" fixed="right">
        <template #default="{ row }">
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="info" size="small" @click="handleResetPassword(row)">重置密码</el-button>
          <el-button type="primary" size="small" @click="openRoleDialog(row)">修改角色</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
        @size-change="loadUsers"
        @current-change="loadUsers"
      />
    </div>

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="showRoleDialog" title="修改用户角色" width="400px">
      <div v-if="currentUser" style="margin-bottom: 16px; color: #606266;">
        用户：{{ currentUser.name || currentUser.phone }}（当前角色：{{ roleText(currentUser.role) }}）
      </div>
      <el-radio-group v-model="newRole">
        <el-radio value="USER">普通用户</el-radio>
        <el-radio value="COLLECTOR">回收员</el-radio>
        <el-radio value="INSTITUTION">机构</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="showRoleDialog = false">取消</el-button>
        <el-button type="primary" :loading="changingRole" @click="handleChangeRole">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, toggleUserStatus, resetUserPassword, changeUserRole, deleteUser } from '../../api/user'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

/* ========== 状态变量 ========== */

const loading = ref(false)
const users = ref([])
const filters = reactive({ keyword: '', role: '', status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

// 修改角色弹窗
const showRoleDialog = ref(false)
const currentUser = ref(null)
const newRole = ref('')
const changingRole = ref(false)

/* ========== 数据加载 ========== */

async function loadUsers() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.role) params.role = filters.role
    if (filters.status !== null && filters.status !== '') params.status = filters.status
    const data = await getUsers(params)
    users.value = data.records || []
    pagination.total = data.total || 0
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadUsers()
}

function handleReset() {
  filters.keyword = ''
  filters.role = ''
  filters.status = null
  pagination.page = 1
  loadUsers()
}

/* ========== 启用/禁用 ========== */

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确认${action}用户「${row.name || row.phone}」？`,
      '操作确认',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await toggleUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`已${action}`)
    loadUsers()
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

/* ========== 重置密码 ========== */

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确认将用户「${row.name || row.phone}」的密码重置为 123456？`,
      '重置密码',
      { confirmButtonText: '确认重置', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

/* ========== 删除用户 ========== */

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除用户「${row.name || row.phone}」？该操作将永久删除该用户及其所有关联数据（订单、地址、积分记录等），不可恢复！`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
  } catch {
    return
  }

  try {
    await deleteUser(row.id)
    ElMessage.success('用户已删除')
    loadUsers()
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

/* ========== 修改角色 ========== */

function openRoleDialog(row) {
  currentUser.value = row
  newRole.value = row.role
  showRoleDialog.value = true
}

async function handleChangeRole() {
  if (newRole.value === currentUser.value.role) {
    ElMessage.info('角色未变更')
    showRoleDialog.value = false
    return
  }

  changingRole.value = true
  try {
    await changeUserRole(currentUser.value.id, newRole.value)
    ElMessage.success('角色修改成功')
    showRoleDialog.value = false
    loadUsers()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    changingRole.value = false
  }
}

/* ========== 导出 Excel ========== */

function handleExport() {
  if (!users.value.length) {
    ElMessage.warning('当前无数据可导出')
    return
  }
  const rows = users.value.map(row => ({
    'ID': row.id,
    '手机号': row.phone,
    '姓名': row.name || '-',
    '角色': roleText(row.role),
    '积分余额': row.pointsBalance ?? 0,
    '状态': row.status === 1 ? '正常' : '禁用',
    '注册时间': formatTime(row.createdAt)
  }))
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '用户列表')
  const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([buf], { type: 'application/octet-stream' }), '用户列表.xlsx')
}

/* ========== 工具函数 ========== */

function roleText(role) {
  const map = { USER: '普通用户', COLLECTOR: '回收员', INSTITUTION: '机构' }
  return map[role] || '未知'
}

function roleTagType(role) {
  const map = { USER: 'info', COLLECTOR: 'warning', INSTITUTION: 'success' }
  return map[role] || 'info'
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

/* ========== 初始化 ========== */

onMounted(() => {
  loadUsers()
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
