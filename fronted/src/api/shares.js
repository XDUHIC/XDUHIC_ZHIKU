import http from '../utils/http'

/**
 * 分享相关API
 */

// 获取分享列表
export function getShares(params) {
  return http.get('/senior-shares', { params })
}

// 获取分享详情
export function getShareDetail(id) {
  return http.get(`/senior-shares/${id}`)
}

// 创建分享
export function createShare(data) {
  return http.post('/senior-shares', data)
}

// 更新分享
export function updateShare(id, data) {
  return http.put(`/senior-shares/${id}`, data)
}

// 删除分享
export function deleteShare(id) {
  return http.delete(`/senior-shares/${id}`)
}

// 获取某分享下的讨论（问题列表）
export function getShareDiscussions(shareId, params) {
  return http.get(`/qna/targets/share/${shareId}/questions`, { params })
}

// 在某分享下发起讨论（创建问题）
export function createShareDiscussion(shareId, data) {
  return http.post(`/qna/targets/share/${shareId}/questions`, data)
}

// 获取某个讨论下的回复
export function getDiscussionAnswers(questionId, params) {
  return http.get(`/qna/questions/${questionId}/answers`, { params })
}

// 回复某个讨论
export function createDiscussionAnswer(questionId, data) {
  return http.post(`/qna/questions/${questionId}/answers`, data)
}