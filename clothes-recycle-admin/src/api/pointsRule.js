/**
 * 积分规则 API 模块
 * 提供积分规则列表查询和更新接口
 */
import request from '../utils/request'

/** 获取所有积分规则 */
export function getPointsRules() {
  return request.get('/api/admin/points-rules')
}

/** 更新积分规则 */
export function updatePointsRule(id, data) {
  return request.put(`/api/admin/points-rules/${id}`, data)
}
