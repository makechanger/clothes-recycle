import request from '../utils/request'

export function getReviews(params) {
  return request.get('/api/admin/reviews', { params })
}
