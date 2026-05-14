import request from '../utils/request'

export function getInstitutionStatistics() {
  return request.get('/api/institution/statistics')
}

export function getInstitutionInfo() {
  return request.get('/api/institution/info')
}

export function updateInstitutionInfo(data) {
  return request.put('/api/institution/info', data)
}

export function getInstitutionOrders(params) {
  return request.get('/api/institution/order/list', { params })
}

export function getInstitutionOrderDetail(id) {
  return request.get(`/api/institution/order/${id}`)
}

export function scanReceiveOrder(orderNo) {
  return request.post('/api/institution/order/receive', { orderNo })
}

export function getUnassignedOrders() {
  return request.get('/api/institution/destination/unassigned-orders')
}

export function assignDestination(data) {
  return request.post('/api/institution/destination/assign', data)
}
