import request from '@/utils/request'

/**
 * 获取微信登录二维码
 * @param {Object} data 请求参数
 */
export const getLoginQrCode = (data) => {
  return request({
    url: '/wx/login-qr',
    method: 'post',
    data
  })
}

/**
 * 检查微信登录状态
 * @param {Object} data 请求参数
 */
export const checkLogin = (data) => {
  return request({
    url: '/wx/check-login',
    method: 'post',
    data
  })
}

/**
 * 查询当前登录用户的微信账号列表
 */
export const listAccounts = () => {
  return request({
    url: '/wx/list',
    method: 'get'
  })
}

/**
 * 删除微信账号
 * @param {number} id 账号 ID
 */
export const deleteAccount = (id) => {
  return request({
    url: `/wx/${id}`,
    method: 'delete'
  })
}

/**
 * 退出微信登录
 * @param {Object} data 请求参数
 */
export const logoutAccount = (data) => {
  return request({
    url: '/wx/logout',
    method: 'post',
    data
  })
}
