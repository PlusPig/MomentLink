import { request } from '@/utils/request'

// 获取内容列表（首页 feed）
export const fetchContents = (params) =>
  request.get('/contents', params)

// 获取内容详情
export const fetchContentDetail = (id) =>
  request.get(`/contents/${id}`)

// 点赞内容
export const likeContent = (id) =>
  request.post(`/contents/${id}/like`)

// 取消点赞内容
export const unlikeContent = (id) =>
  request({
    url: `/contents/${id}/like`,
    method: 'DELETE',
  })

// 评分内容（使用 ContentController 上的评分接口，通过 query 传 score）
export const rateContentOnContent = (id, score) =>
  request.post(`/contents/${id}/rating?score=${score}`)
