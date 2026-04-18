import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'
import router from '../router'

// 管理员状态管理（登录信息持久化到 localStorage）
export const useAdminStore = defineStore('admin', () => {
  // 从 localStorage 恢复登录状态
  const token = ref(localStorage.getItem('admin_token') || '')
  const username = ref(localStorage.getItem('admin_username') || '')
  const role = ref(localStorage.getItem('admin_role') || '')

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 管理员登录
  async function login(usernameInput, password) {
    const data = await request.post('/api/admin/login', {
      username: usernameInput,
      password: password
    })
    // 存储到 state
    token.value = data.token
    username.value = data.username
    role.value = data.role
    // 持久化到 localStorage
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_username', data.username)
    localStorage.setItem('admin_role', data.role)
  }

  // 退出登录
  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    localStorage.removeItem('admin_role')
    router.push('/login')
  }

  return { token, username, role, isLoggedIn, login, logout }
})
