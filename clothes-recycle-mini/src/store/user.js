/**
 * 用户状态管理（Pinia）
 * 存储当前登录用户的信息和角色，用于角色路由切换
 * 角色值：USER（普通用户）、COLLECTOR（回收员）、INSTITUTION（机构）
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // ===== 状态 =====

  // 登录 token
  const token = ref(uni.getStorageSync('token') || '')

  // 用户信息（从后端获取）
  const userInfo = ref(uni.getStorageSync('userInfo') || null)

  // 当前角色：USER / COLLECTOR / INSTITUTION
  const role = ref(uni.getStorageSync('role') || '')

  // ===== 计算属性 =====

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 是否是普通用户
  const isUser = computed(() => role.value === 'USER')

  // 是否是回收员
  const isCollector = computed(() => role.value === 'COLLECTOR')

  // 是否是机构
  const isInstitution = computed(() => role.value === 'INSTITUTION')

  // ===== 方法 =====

  /**
   * 登录成功后保存信息
   * @param {Object} data - { token, userInfo, role }
   */
  function login(data) {
    token.value = data.token
    userInfo.value = data.userInfo
    role.value = data.role

    // 持久化到本地存储
    uni.setStorageSync('token', data.token)
    uni.setStorageSync('userInfo', data.userInfo)
    uni.setStorageSync('role', data.role)
  }

  /**
   * 退出登录，清除所有状态
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    role.value = ''

    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('role')

    // 跳转到首页（不强制登录）
    uni.reLaunch({ url: '/pages/index/index' })
  }

  /**
   * 更新用户信息（如绑定手机号后）
   */
  function updateUserInfo(info) {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  return {
    token, userInfo, role,
    isLoggedIn, isUser, isCollector, isInstitution,
    login, logout, updateUserInfo
  }
})
