<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">衣物回收管理后台</h2>

      <el-tabs v-model="loginType" class="login-tabs" stretch>
        <el-tab-pane label="管理员登录" name="admin" />
        <el-tab-pane label="机构登录" name="institution" />
      </el-tabs>

      <!-- 管理员登录表单 -->
      <el-form
        v-if="loginType === 'admin'"
        ref="adminFormRef"
        :model="adminForm"
        :rules="adminRules"
        label-width="0"
        @keyup.enter="handleAdminLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="adminForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="adminForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleAdminLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 机构登录表单 -->
      <el-form
        v-else
        ref="instFormRef"
        :model="instForm"
        :rules="instRules"
        label-width="0"
        @keyup.enter="handleInstLogin"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="instForm.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="instForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleInstLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { useAdminStore } from '../../store/admin'
import { useInstitutionStore } from '../../store/institution'

const router = useRouter()
const adminStore = useAdminStore()
const institutionStore = useInstitutionStore()

const loginType = ref('admin')
const loading = ref(false)

const adminFormRef = ref(null)
const instFormRef = ref(null)

const adminForm = reactive({ username: '', password: '' })
const instForm = reactive({ phone: '', password: '' })

const adminRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const instRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleAdminLogin() {
  const valid = await adminFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await adminStore.login(adminForm.username, adminForm.password)
    router.push('/dashboard')
  } catch (e) {
    // 错误提示由 request.js 拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function handleInstLogin() {
  const valid = await instFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await institutionStore.login(instForm.phone, instForm.password)
    router.push('/inst-dashboard')
  } catch (e) {
    // 错误提示由 request.js 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 20px 30px;
  border-radius: 12px;
}

.login-title {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
  font-size: 22px;
}

.login-tabs {
  margin-bottom: 10px;
}

.login-btn {
  width: 100%;
}
</style>
