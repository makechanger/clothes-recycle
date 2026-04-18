<template>
  <!-- 登录页：居中卡片布局 -->
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">衣物回收管理后台</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        @keyup.enter="handleLogin"
      >
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
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
import { User, Lock } from '@element-plus/icons-vue'
import { useAdminStore } from '../../store/admin'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: ''
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 登录处理
async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await adminStore.login(form.username, form.password)
    router.push('/dashboard')
  } catch (e) {
    // 错误提示由 request.js 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 登录页全屏居中背景 */
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 登录卡片 */
.login-card {
  width: 420px;
  padding: 20px 30px;
  border-radius: 12px;
}

/* 标题 */
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 22px;
}

/* 登录按钮撑满宽度 */
.login-btn {
  width: 100%;
}
</style>
