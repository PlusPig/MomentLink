import { request } from '@/utils/request'

// 获取通知列表
export const fetchNotifications = (params) =>
  request.get('/notifications', params)

// 获取未读数量
export const fetchUnreadCount = () => request.get('/notifications/unread/count')

// 标记单条为已读
export const markNotificationRead = (id) =>
  request({
    url: `/notifications/${id}/read`,
    method: 'PUT',
  })

// 全部标记为已读
export const markAllNotificationsRead = () =>
  request({
    url: '/notifications/read/all',
    method: 'PUT',
  })

// 删除通知
export const deleteNotification = (id) =>
  request({
    url: `/notifications/${id}`,
    method: 'DELETE',
  })


