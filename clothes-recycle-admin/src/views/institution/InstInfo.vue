<template>
  <div class="info-page">
    <el-card shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">机构信息</span>
          <el-button v-if="!editing" type="primary" @click="startEdit">编辑</el-button>
        </div>
      </template>

      <!-- 查看模式 -->
      <el-descriptions v-if="!editing" :column="1" border>
        <el-descriptions-item label="机构名称">{{ info.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="机构类型">{{ info.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ info.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ info.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="info.status === 1 ? 'success' : 'danger'">
            {{ info.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(info.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 编辑模式 -->
      <el-form v-else :model="editForm" label-width="100px">
        <el-form-item label="机构名称" required>
          <el-input v-model="editForm.name" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="机构类型">
          <el-select v-model="editForm.type" placeholder="请选择机构类型" style="width: 100%;">
            <el-option label="环保机构" value="环保机构" />
            <el-option label="慈善机构" value="慈善机构" />
            <el-option label="回收企业" value="回收企业" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="editForm.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editForm.address" type="textarea" :rows="2" placeholder="请输入机构地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInstitutionInfo, updateInstitutionInfo } from '../../api/institution'

const loading = ref(false)
const info = ref({})
const editing = ref(false)
const saving = ref(false)
const editForm = reactive({ name: '', type: '', contactPerson: '', address: '' })

async function loadInfo() {
  loading.value = true
  try {
    info.value = await getInstitutionInfo() || {}
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editForm.name = info.value.name || ''
  editForm.type = info.value.type || ''
  editForm.contactPerson = info.value.contactPerson || ''
  editForm.address = info.value.address || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function handleSave() {
  if (!editForm.name.trim()) {
    ElMessage.warning('机构名称不能为空')
    return
  }
  saving.value = true
  try {
    await updateInstitutionInfo({
      name: editForm.name.trim(),
      type: editForm.type,
      contactPerson: editForm.contactPerson.trim(),
      address: editForm.address.trim()
    })
    ElMessage.success('保存成功')
    editing.value = false
    loadInfo()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    saving.value = false
  }
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadInfo()
})
</script>

<style scoped>
.info-page {
  padding: 20px;
  max-width: 800px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
