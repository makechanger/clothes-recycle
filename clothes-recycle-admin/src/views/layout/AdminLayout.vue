<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">{{ isInstitution ? '机构管理后台' : '衣物回收后台' }}</div>
      <el-menu :default-active="currentRoute" router background-color="#304156" text-color="#bfcbd9"
        active-text-color="#409eff">

        <!-- ========== 管理员菜单 ========== -->
        <template v-if="!isInstitution">
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/collectors">
            <el-icon><UserFilled /></el-icon>
            <span>回收员管理</span>
          </el-menu-item>
          <el-menu-item index="/institutions">
            <el-icon><OfficeBuilding /></el-icon>
            <span>机构管理</span>
          </el-menu-item>
          <el-menu-item v-if="adminStore.isSuperAdmin" index="/admins">
            <el-icon><Key /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>
          <el-menu-item index="/reviews">
            <el-icon><ChatLineSquare /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
          <el-menu-item index="/complaints">
            <el-icon><Warning /></el-icon>
            <span>申诉管理</span>
          </el-menu-item>
          <el-menu-item index="/points-rules">
            <el-icon><Setting /></el-icon>
            <span>积分规则</span>
          </el-menu-item>
        </template>

        <!-- ========== 机构菜单 ========== -->
        <template v-else>
          <el-menu-item index="/inst-dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/inst-orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/inst-receive">
            <el-icon><Promotion /></el-icon>
            <span>扫码接收</span>
          </el-menu-item>
          <el-menu-item index="/inst-info">
            <el-icon><OfficeBuilding /></el-icon>
            <span>机构信息</span>
          </el-menu-item>
        </template>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="header-title">{{ isInstitution ? '机构后台' : '管理后台' }}</span>
        <div class="header-right">
          <span class="admin-name">{{ displayName }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataBoard, List, UserFilled, Setting, User, ChatLineSquare, Warning, Key, Promotion, OfficeBuilding } from '@element-plus/icons-vue'
import { useAdminStore } from '../../store/admin'
import { useInstitutionStore } from '../../store/institution'

const route = useRoute()
const adminStore = useAdminStore()
const institutionStore = useInstitutionStore()

const isInstitution = computed(() => institutionStore.isLoggedIn)
const displayName = computed(() => isInstitution.value ? institutionStore.institutionName : adminStore.username)
const currentRoute = computed(() => route.path)

function handleLogout() {
  if (isInstitution.value) {
    institutionStore.logout()
  } else {
    adminStore.logout()
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #3a4a5b;
}

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

.main {
  background: #f0f2f5;
}
</style>
