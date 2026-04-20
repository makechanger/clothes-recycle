/**
 * 数据统计 API 模块
 * 获取管理后台仪表盘统计数据
 */
import request from '../utils/request'

/** 获取统计概览数据 */
export function getStatistics() {
  return request.get('/api/admin/statistics')
}
