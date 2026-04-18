/**
 * 订单管理 API 模块
 * 提供管理员查看订单列表（分页+筛选）和订单详情接口
 */
import request from '../utils/request'

/** 分页查询订单列表 */
export function getOrders(params) {
  return request.get('/api/admin/orders', { params })
}

/** 查询订单详情（含状态流转日志） */
export function getOrderDetail(id) {
  return request.get(`/api/admin/orders/${id}`)
}
