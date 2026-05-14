/**
 * 机构管理 API 模块（管理员侧）
 * 提供机构列表、机构相关资质申请审批等接口调用
 */
import request from '../utils/request'

/** 获取机构列表 */
export function getInstitutions() {
  return request.get('/api/admin/institutions')
}

/** 获取待审核的机构申请列表 */
export function getPendingApplications() {
  return request.get('/api/admin/applications/pending', { params: { applyRole: 'INSTITUTION' } })
}

/** 获取已处理的机构审批记录 */
export function getProcessedApplications() {
  return request.get('/api/admin/applications/processed', { params: { applyRole: 'INSTITUTION' } })
}

/** 审批通过 */
export function approveApplication(id) {
  return request.post(`/api/admin/applications/${id}/approve`)
}

/** 审批拒绝 */
export function rejectApplication(id, reason) {
  return request.post(`/api/admin/applications/${id}/reject`, { reason })
}
