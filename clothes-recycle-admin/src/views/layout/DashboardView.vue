<template>
  <!-- 仪表盘占位页 -->
  <div class="dashboard">
    <el-card>
      <h2>欢迎使用衣物回收管理后台</h2>
      <p class="desc">当前登录管理员：{{ adminStore.username }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { useAdminStore } from '../../store/admin'

const adminStore = useAdminStore()
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard h2 {
  margin-bottom: 12px;
  color: #303133;
}

.desc {
  color: #909399;
  font-size: 14px;
}
</style>
