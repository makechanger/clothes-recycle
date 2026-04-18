<template>
  <!-- 后台骨架布局：侧边栏 + 顶栏 + 内容区 -->
  <el-container class="admin-layout">
    <!-- 左侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">衣物回收后台</div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <!-- 仪表盘 -->
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/collectors">
          <el-icon><UserFilled /></el-icon>
          <span>回收员/机构管理</span>
        </el-menu-item>
        <el-menu-item index="/points-rules">
          <el-icon><Setting /></el-icon>
          <span>积分规则</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/reviews">
          <el-icon><ChatLineSquare /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/complaints">
          <el-icon><Warning /></el-icon>
          <span>申诉管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <span class="header-title">管理后台</span>
        <div class="header-right">
          <span class="admin-name">{{ adminStore.username }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataBoard, List, UserFilled, Setting, User, ChatLineSquare, Warning } from '@element-plus/icons-vue'
import { useAdminStore } from '../../store/admin'

const route = useRoute()
const adminStore = useAdminStore()

// 当前路由路径（用于侧边栏高亮）
const currentRoute = computed(() => route.path)

// 退出登录
function handleLogout() {
  adminStore.logout()
}
</script>

<style scoped>
/* 整体布局撑满屏幕 */
.admin-layout {
  height: 100vh;
}

/* 侧边栏 */
.aside {
  background-color: #304156;
  overflow-y: auto;
}

/* Logo 区域 */
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #3a4a5b;
}

/* 顶栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-name {
  color: #606266;
  font-size: 14px;
}

/* 内容区 */
.main {
  background: #f0f2f5;
}
</style>
