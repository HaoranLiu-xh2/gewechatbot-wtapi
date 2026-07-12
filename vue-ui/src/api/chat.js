/**
 * 聊天相关 API
 */
import request from '@/utils/request'

/**
 * 获取当前用户的微信账号列表
 */
export const listWxAccounts = () => {
  return request({
    url: '/wx/list',
    method: 'get'
  })
}

/**
 * 获取联系人列表
 * @param {string} ownerWxid 当前登录微信的 wxid
 */
export const listContacts = (ownerWxid) => {
  return request({
    url: '/wx/contact/list',
    method: 'get',
    params: { ownerWxid }
  })
}

/**
 * 获取历史消息
 * @param {Object} params 查询参数
 */
export const listMessages = (params) => {
  return request({
    url: '/wx/message/list',
    method: 'get',
    params
  })
}

/**
 * 发送文本消息
 * @param {Object} data 发送参数
 */
export const sendTextMessage = (data) => {
  return request({
    url: '/wx/message/send-text',
    method: 'post',
    data
  })
}

/**
 * 上传文件到对象存储
 * @param {File} file 文件对象
 * @param {string} prefix 目录前缀
 */
export const uploadFile = (file, prefix = 'wx') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('prefix', prefix)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 发送图片消息
 * @param {Object} data 发送参数
 */
export const sendImageMessage = (data) => {
  return request({
    url: '/wx/message/send-image',
    method: 'post',
    data
  })
}

/**
 * 发送文件消息
 * @param {Object} data 发送参数
 */
export const sendFileMessage = (data) => {
  return request({
    url: '/wx/message/send-file',
    method: 'post',
    data
  })
}

/**
 * 发送视频消息
 * @param {Object} data 发送参数
 */
export const sendVideoMessage = (data) => {
  return request({
    url: '/wx/message/send-video',
    method: 'post',
    data
  })
}

/**
 * 下载图片消息
 * @param {Object} data 下载参数 { messageId, type }
 */
export const downloadImage = (data) => {
  return request({
    url: '/wx/message/download-image',
    method: 'post',
    data
  })
}
