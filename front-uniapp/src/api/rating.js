import { request } from '@/utils/request'

// 使用独立 RatingController 接口

// 为内容评分
export const rateContent = (payload) =>
  request.post('/ratings', payload)

// 获取我的评分
export const fetchMyRatings = (params) =>
  request.get('/ratings/my', params)

// 获取某内容评分详情
export const fetchContentRatings = (contentId) =>
  request.get(`/ratings/content/${contentId}`)

// 删除某内容评分
export const deleteRating = (contentId) =>
  request({
    url: `/ratings/${contentId}`,
    method: 'DELETE',
  })

