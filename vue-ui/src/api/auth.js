import request from '@/utils/request'

/**
 * 登录
 * @param {Object} data 登录参数
 */
export const login = (data) => {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

/**
 * 注册
 * @param {Object} data 注册参数
 */
export const register = (data) => {
  return request({
    url: '/register',
    method: 'post',
    data
  })
}
