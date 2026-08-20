import request from '@/utils/request'

/**
 * 创建群发任务
 * @param {Object} data 任务参数
 */
export const createMassTask = (data) => {
  return request({
    url: '/mass-task/create',
    method: 'post',
    data
  })
}

/**
 * 分页查询群发任务列表
 * @param {Object} params 分页参数
 */
export const pageMassTasks = (params) => {
  return request({
    url: '/mass-task/page',
    method: 'get',
    params
  })
}

/**
 * 查询任务详情
 * @param {number} id 任务 ID
 */
export const getMassTaskDetail = (id) => {
  return request({
    url: `/mass-task/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询任务发送记录
 * @param {number} id 任务 ID
 * @param {Object} params 分页参数
 */
export const pageMassTaskRecords = (id, params) => {
  return request({
    url: `/mass-task/${id}/records`,
    method: 'get',
    params
  })
}

/**
 * 暂停任务
 * @param {number} id 任务 ID
 */
export const pauseMassTask = (id) => {
  return request({
    url: `/mass-task/${id}/pause`,
    method: 'post'
  })
}

/**
 * 取消任务
 * @param {number} id 任务 ID
 */
export const cancelMassTask = (id) => {
  return request({
    url: `/mass-task/${id}/cancel`,
    method: 'post'
  })
}

/**
 * 删除任务
 * @param {number} id 任务 ID
 */
export const deleteMassTask = (id) => {
  return request({
    url: `/mass-task/${id}`,
    method: 'delete'
  })
}
