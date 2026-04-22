<template>
  <div class="admin-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="用户名搜索"
        clearable
        style="width: 200px;"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="openCreateDialog">新增管理员</el-button>
    </div>

    <!-- 管理员表格 -->
    <el-table :data="admins" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column label="角色" min-width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">
            {{ row.role === 'admin' ? '超级管理员' : '操作员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="240" fixed="right">
        <template #default="{ row }">
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="info" size="small" @click="handleResetPassword(row)">重置密码</el-button>
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
        @size-change="loadAdmins"
        @current-change="loadAdmins"
      />
    </div>

    <!-- 新增管理员弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新增管理员" width="420px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="createForm.password" type="password" show-password placeholder="留空则默认 admin123" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="createForm.role">
            <el-radio value="operator">操作员</el-radio>
            <el-radio value="admin">超级管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdmins, createAdmin, toggleAdminStatus, resetAdminPassword, deleteAdmin } from '../../api/adminManager'

const loading = ref(false)
const admins = ref([])
const filters = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const showCreateDialog = ref(false)
const creating = ref(false)
const createForm = reactive({ username: '', password: '', role: 'operator' })

async function loadAdmins() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (filters.keyword) params.keyword = filters.keyword
    const data = await getAdmins(params)
    admins.value = data.records || []
    pagination.total = data.total || 0
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadAdmins()
}

function handleReset() {
  filters.keyword = ''
  pagination.page = 1
  loadAdmins()
}

function openCreateDialog() {
  createForm.username = ''
  createForm.password = ''
  createForm.role = 'operator'
  showCreateDialog.value = true
}

async function handleCreate() {
  if (!createForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  creating.value = true
  try {
    await createAdmin({
      username: createForm.username.trim(),
      password: createForm.password || undefined,
      role: createForm.role
    })
    ElMessage.success('管理员创建成功')
    showCreateDialog.value = false
    loadAdmins()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    creating.value = false
  }
}

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确认${action}管理员「${row.username}」？`,
      '操作确认',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await toggleAdminStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`已${action}`)
    loadAdmins()
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确认将管理员「${row.username}」的密码重置为 admin123？`,
      '重置密码',
      { confirmButtonText: '确认重置', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await resetAdminPassword(row.id)
    ElMessage.success('密码已重置为 admin123')
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除管理员「${row.username}」？此操作不可恢复！`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
  } catch {
    return
  }
  try {
    await deleteAdmin(row.id)
    ElMessage.success('管理员已删除')
    loadAdmins()
  } catch (e) {
    // 错误由 request.js 统一处理
  }
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadAdmins()
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
