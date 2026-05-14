import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例，baseURL 为空（通过 Vite 代理转发 /api 请求到后端）
const request = axios.create({
  timeout: 15000
})

// 判断当前登录角色
export function getLoginRole() {
  if (localStorage.getItem('admin_token')) return 'admin'
  if (localStorage.getItem('inst_token')) return 'institution'
  return null
}

// 请求拦截器：根据登录角色注入对应 token
request.interceptors.request.use(config => {
  const role = getLoginRole()
  const token = role === 'institution'
    ? localStorage.getItem('inst_token')
    : localStorage.getItem('admin_token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

// 清除所有登录状态
function clearAllAuth() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_username')
  localStorage.removeItem('admin_role')
  localStorage.removeItem('inst_token')
  localStorage.removeItem('inst_name')
  localStorage.removeItem('inst_phone')
  localStorage.removeItem('inst_user_id')
  localStorage.removeItem('inst_id')
  localStorage.removeItem('inst_inst_name')
}

// 响应拦截器：解包 Result<T> 结构 + 统一错误处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      clearAllAuth()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        clearAllAuth()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (data && data.message) {
        ElMessage.error(data.message)
      } else {
        ElMessage.error(`请求失败 (${status})`)
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
