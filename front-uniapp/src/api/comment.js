import { request } from '@/utils/request'

// 获取某内容的评论列表
export const fetchCommentsByContent = (contentId, params) =>
  request.get(`/comments/content/${contentId}`, params)

// 发表评论
export const createComment = (payload) =>
  request.post('/comments', payload)

// 点赞评论
export const likeComment = (id) =>
  request.post(`/comments/${id}/like`)

// 删除评论
export const deleteComment = (id) =>
  request({
    url: `/comments/${id}`,
    method: 'DELETE',
  })


