/**
 * 统一网络请求封装
 * 功能：自动携带 token、统一错误处理、loading 控制
 */

const BASE_URL = 'http://47.116.79.134:8080'
// const BASE_URL = 'http://localhost:8080'
/**
 * 发起请求
 * @param {Object} options - 请求配置
 * @param {string} options.url - 接口路径（如 /api/user/login）
 * @param {string} options.method - 请求方法，默认 GET
 * @param {Object} options.data - 请求参数
 * @param {boolean} options.loading - 是否显示加载提示，默认 true
 * @returns {Promise} 返回后端 Result 中的 data 字段
 */
const request = (options) => {
  const { url, method = 'GET', data = {}, loading = true } = options
  // console.log('【实际请求地址】', BASE_URL + url)
  // console.log('【请求方法】', method)
  // console.log('【请求参数】', data)

  // 显示加载提示
  if (loading) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  // 从本地缓存读取 token
  const token = uni.getStorageSync('token')

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token || ''
      },
      success: (res) => {
        if (loading) uni.hideLoading()

        const result = res.data

        // 后端返回 200 表示业务成功
        if (result.code === 200) {
          resolve(result.data)
        }
        // 401 未登录，跳转登录页
        else if (result.code === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(result)
        }
        // 其他业务错误，弹出提示
        else {
          uni.showToast({ title: result.message || '请求失败', icon: 'none' })
          reject(result)
        }
      },
      fail: (err) => {
        if (loading) uni.hideLoading()
        uni.showToast({ title: '网络错误，请检查网络', icon: 'none' })
        reject(err)
      }
    })
  })
}

/**
 * 上传文件（图片等）
 * @param {string} filePath - 本地文件路径
 * @returns {Promise<string>} 返回服务器文件 URL
 */
const uploadFile = (filePath) => {
  const token = uni.getStorageSync('token')

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/api/common/upload',
      filePath,
      name: 'file',
      header: {
        'Authorization': token || ''
      },
      success: (res) => {
        const result = JSON.parse(res.data)
        if (result.code === 200) {
          resolve(result.data)
        } else {
          uni.showToast({ title: result.message || '上传失败', icon: 'none' })
          reject(result)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export { request, uploadFile, BASE_URL }
export default request
