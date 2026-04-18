import request from '../utils/request'

export function getComplaints(params) {
  return request.get('/api/admin/complaints', { params })
}

export function handleComplaint(id, data) {
  return request.post(`/api/admin/complaints/${id}/handle`, data)
}
