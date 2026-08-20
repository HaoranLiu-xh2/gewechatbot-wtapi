import request from '@/utils/request'

/**
 * 分页查询素材列表
 * @param {Object} params 查询参数
 */
export const pageList = (params) => {
  return request({
    url: '/material/page',
    method: 'get',
    params
  })
}

/**
 * 查询素材详情
 * @param {number} id 素材 ID
 */
export const getById = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'get'
  })
}

/**
 * 新增素材
 * @param {Object} data 素材信息
 */
export const addMaterial = (data) => {
  return request({
    url: '/material',
    method: 'post',
    data
  })
}

/**
 * 修改素材
 * @param {Object} data 素材信息
 */
export const updateMaterial = (data) => {
  return request({
    url: '/material',
    method: 'put',
    data
  })
}

/**
 * 删除素材
 * @param {number} id 素材 ID
 */
export const deleteMaterial = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'delete'
  })
}

/**
 * 将聊天消息保存为素材
 * @param {number} messageId 消息 ID
 */
export const saveMessageToMaterial = (messageId) => {
  return request({
    url: '/material/save-from-message',
    method: 'post',
    params: { messageId }
  })
}
