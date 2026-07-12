import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // Token
  const token = ref(localStorage.getItem('token') || '')
  // 用户名
  const username = ref(localStorage.getItem('username') || '')
  // 微信 API Token（X-finder-TOKEN）
  const wxApiToken = ref(localStorage.getItem('wxApiToken') || '')

  /**
   * 设置登录信息
   */
  const setUserInfo = (newToken, newUsername, newWxApiToken = '') => {
    token.value = newToken
    username.value = newUsername
    wxApiToken.value = newWxApiToken
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('wxApiToken', newWxApiToken)
  }

  /**
   * 设置微信 API Token
   */
  const setWxApiToken = (newWxApiToken) => {
    wxApiToken.value = newWxApiToken
    localStorage.setItem('wxApiToken', newWxApiToken)
  }

  /**
   * 清除登录信息
   */
  const clearUserInfo = () => {
    token.value = ''
    username.value = ''
    wxApiToken.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('wxApiToken')
  }

  return { token, username, wxApiToken, setUserInfo, setWxApiToken, clearUserInfo }
})
