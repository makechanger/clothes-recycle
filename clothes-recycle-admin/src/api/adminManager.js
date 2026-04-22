import request from '../utils/request'

/** 分页查询管理员列表 */
export function getAdmins(params) {
  return request.get('/api/admin/admins', { params })
}

/** 新增管理员 */
export function createAdmin(data) {
  return request.post('/api/admin/admins/create', data)
}

/** 启用/禁用管理员 */
export function toggleAdminStatus(id, status) {
  return request.put(`/api/admin/admins/${id}/status`, { status })
}

/** 重置管理员密码 */
export function resetAdminPassword(id) {
  return request.post(`/api/admin/admins/${id}/reset-password`)
}

/** 删除管理员 */
export function deleteAdmin(id) {
  return request.delete(`/api/admin/admins/${id}`)
}
