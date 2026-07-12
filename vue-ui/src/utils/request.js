import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    // 自动携带 Token
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务成功
    if (res.code === 200) {
      return res
    }
    // 业务失败
    if (res.code === 1003) {
      ElMessage.error(res.msg || '用户不存在，请重新登录')
      const userStore = useUserStore()
      userStore.clearUserInfo()
      router.push('/login')
      return Promise.reject(new Error(res.msg || '用户不存在'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    const { response } = error
    if (response) {
      // 401 未登录或 Token 失效
      if (response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.clearUserInfo()
        router.push('/login')
      } else {
        ElMessage.error(response.data?.msg || '服务器异常')
      }
    } else {
      ElMessage.error('网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
