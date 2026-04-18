<template>
  <!-- 积分规则管理页面：规则表格 + 编辑弹窗 -->
  <div class="points-rule-page">
    <el-table :data="rules" v-loading="loading" stripe>
      <el-table-column prop="clothesCategory" label="衣物分类" min-width="120" />
      <el-table-column prop="pointsPerKg" label="每公斤积分" min-width="120" />
      <el-table-column label="最低重量(kg)" min-width="120">
        <template #default="{ row }">
          {{ row.minWeight ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑积分规则" width="420px" @close="resetEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="衣物分类">
          <el-input :model-value="editForm.clothesCategory" disabled />
        </el-form-item>
        <el-form-item label="每公斤积分" prop="pointsPerKg">
          <el-input-number v-model="editForm.pointsPerKg" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="最低重量(kg)" prop="minWeight">
          <el-input-number v-model="editForm.minWeight" :min="0" :max="999" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="editForm.statusBool"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPointsRules, updatePointsRule } from '../../api/pointsRule'

/* ========== 状态变量 ========== */

const loading = ref(false)
const rules = ref([])

const showEditDialog = ref(false)
const editFormRef = ref(null)
const saving = ref(false)
const currentEditId = ref(null)
const editForm = reactive({
  clothesCategory: '',
  pointsPerKg: 0,
  minWeight: 0,
  statusBool: true
})
const editRules = {
  pointsPerKg: [
    { required: true, message: '请输入每公斤积分', trigger: 'blur' }
  ],
  minWeight: [
    { required: true, message: '请输入最低重量', trigger: 'blur' }
  ]
}

/* ========== 数据加载 ========== */

async function loadRules() {
  loading.value = true
  try {
    rules.value = await getPointsRules()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    loading.value = false
  }
}

/* ========== 编辑操作 ========== */

function handleEdit(row) {
  currentEditId.value = row.id
  editForm.clothesCategory = row.clothesCategory
  editForm.pointsPerKg = row.pointsPerKg
  editForm.minWeight = row.minWeight
  editForm.statusBool = row.status === 1
  showEditDialog.value = true
}

async function handleSave() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updatePointsRule(currentEditId.value, {
      pointsPerKg: editForm.pointsPerKg,
      minWeight: editForm.minWeight,
      status: editForm.statusBool ? 1 : 0
    })
    ElMessage.success('积分规则更新成功')
    showEditDialog.value = false
    loadRules()
  } catch (e) {
    // 错误由 request.js 统一处理
  } finally {
    saving.value = false
  }
}

function resetEditForm() {
  editForm.clothesCategory = ''
  editForm.pointsPerKg = 0
  editForm.minWeight = 0
  editForm.statusBool = true
  currentEditId.value = null
}

/* ========== 初始化 ========== */

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.points-rule-page {
  padding: 0;
}
</style>
