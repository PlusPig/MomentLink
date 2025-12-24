export const getBaseUrl = () => {
  const isH5 = process.env.UNI_PLATFORM === 'h5'
  const isDev = process.env.NODE_ENV !== 'production'

  if (isH5 && isDev) return '/api'
  return 'http://localhost:8088/api'
}

const getAuthHeader = () => {
  const authToken = uni.getStorageSync('authToken')
  return authToken ? { Authorization: authToken } : {}
}

export const request = (options = {}) => {
  const baseUrl = getBaseUrl()

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${baseUrl}${options.url || ''}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(options.auth === false ? {} : getAuthHeader()),
        ...(options.header || {}),
      },
      timeout: options.timeout || 15000,
      success: (res) => {
        const { statusCode, data } = res || {}

        // 检查响应数据是否包含 Result 对象（有 code 字段）
        if (data && typeof data === 'object' && 'code' in data) {
          if (data.code === 200) {
            resolve(data)
            return
          }

          // 处理错误响应
          let errorMessage = data.message || '请求失败'
          if (data.data && typeof data.data === 'object') {
            // 如果是参数校验错误，提取字段错误信息
            const fieldErrors = Object.values(data.data).join('; ')
            if (fieldErrors) {
              errorMessage = fieldErrors
            }
          }
          reject(new Error(errorMessage))
          return
        }

        // 如果没有 Result 格式，检查 HTTP 状态码
        if (statusCode !== 200) {
          if (data && typeof data === 'object' && 'message' in data) {
            reject(new Error(data.message || `HTTP ${statusCode}`))
            return
          }
          reject(new Error(`HTTP ${statusCode}`))
          return
        }

        resolve(data)
      },
      fail: (err) => {
        reject(err)
      },
    })
  })
}

request.get = (url, data, options = {}) => request({ ...options, url, data, method: 'GET' })
request.post = (url, data, options = {}) => request({ ...options, url, data, method: 'POST' })
request.put = (url, data, options = {}) => request({ ...options, url, data, method: 'PUT' })
request.delete = (url, data, options = {}) => request({ ...options, url, data, method: 'DELETE' })
