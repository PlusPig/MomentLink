import { request, getBaseUrl } from '@/utils/request'

// 获取当前用户信息
export const getCurrentUser = () => request.get('/users/me')

// 获取指定用户信息
export const getUserById = (id) => request.get(`/users/${id}`)

// 更新用户资料
export const updateProfile = (payload) => request.put('/users/profile', payload)

// 上传头像
export const uploadAvatar = (filePath) => {
  const baseUrl = getBaseUrl()
  const token = uni.getStorageSync('authToken') || ''
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${baseUrl}/users/avatar`,
      header: {
        Authorization: token,
      },
      filePath,
      name: 'file',
      success: (res) => {
        try {
          const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (data && data.code === 200) {
            resolve(data)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: reject,
    })
  })
}

// 修改密码
export const updatePassword = (payload) => request.put('/users/password', payload)

// 获取我的内容列表
export const fetchMyContents = (params) => request.get('/contents/my', params)

// 更新内容
export const updateContent = (id, payload) => request.put(`/contents/${id}`, payload)

// 删除内容
export const deleteContent = (id) =>
  request({
    url: `/contents/${id}`,
    method: 'DELETE',
  })

