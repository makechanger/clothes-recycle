/**
 * 用户管理 API 模块
 * 提供用户列表查询、启用/禁用、重置密码、修改角色等接口调用
 */
import request from '../utils/request'

/** 分页查询用户列表 */
export function getUsers(params) {
  return request.get('/api/admin/users', { params })
}

/** 启用/禁用用户 */
export function toggleUserStatus(id, status) {
  return request.put(`/api/admin/users/${id}/status`, { status })
}

/** 重置用户密码 */
export function resetUserPassword(id) {
  return request.post(`/api/admin/users/${id}/reset-password`)
}

/** 修改用户角色 */
export function changeUserRole(id, role) {
  return request.put(`/api/admin/users/${id}/role`, { role })
}

/** 删除用户（软删除） */
export function deleteUser(id) {
  return request.delete(`/api/admin/users/${id}`)
}
