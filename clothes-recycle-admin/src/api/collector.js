/**
 * 回收员管理 API 模块
 * 提供回收员列表、资质申请审批等接口调用
 */
import request from '../utils/request'

/** 获取回收员列表 */
export function getCollectors() {
  return request.get('/api/admin/collectors')
}

/** 获取待审核的回收员申请列表 */
export function getPendingApplications() {
  return request.get('/api/admin/applications/pending', { params: { applyRole: 'COLLECTOR' } })
}

/** 审批通过 */
export function approveApplication(id) {
  return request.post(`/api/admin/applications/${id}/approve`)
}

/** 审批拒绝 */
export function rejectApplication(id, reason) {
  return request.post(`/api/admin/applications/${id}/reject`, { reason })
}

/** 获取已处理的回收员审批记录 */
export function getProcessedApplications() {
  return request.get('/api/admin/applications/processed', { params: { applyRole: 'COLLECTOR' } })
}
