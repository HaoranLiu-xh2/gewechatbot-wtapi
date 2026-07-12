import request from '@/utils/request'

/**
 * 查询当前登录用户详情
 */
export const getCurrentUser = () => {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

/**
 * 更新当前登录用户的微信 API Token
 * @param {Object} data Token 参数
 */
export const updateToken = (data) => {
  return request({
    url: '/user/token',
    method: 'put',
    data
  })
}

/**
 * 分页查询用户列表
 * @param {Object} params 查询参数
 */
export const pageList = (params) => {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

/**
 * 查询用户详情
 * @param {number} id 用户 ID
 */
export const getById = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

/**
 * 新增用户
 * @param {Object} data 用户信息
 */
export const addUser = (data) => {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

/**
 * 修改用户
 * @param {Object} data 用户信息
 */
export const updateUser = (data) => {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id 用户 ID
 */
export const deleteUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}
