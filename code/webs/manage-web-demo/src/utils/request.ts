import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'
import router from './auth'

const request: AxiosInstance = axios.create({
  baseURL: '/ai-studio/v1',
  timeout: 30000,
})

let pendingCount = 0
let loadingTimer: number | undefined
let loadingInstance: LoadingInstance | null = null

function startGlobalLoading() {
  pendingCount += 1
  if (pendingCount === 1) {
    loadingTimer = window.setTimeout(() => {
      const target = document.querySelector('.main-content')
      loadingInstance = ElLoading.service({
        target: target instanceof HTMLElement ? target : document.body,
        lock: true,
        text: 'Loading...',
        background: 'rgba(255, 255, 255, 0.6)',
      })
    }, 180)
  }
}

function stopGlobalLoading() {
  pendingCount = Math.max(0, pendingCount - 1)
  if (pendingCount > 0) return
  if (loadingTimer) {
    window.clearTimeout(loadingTimer)
    loadingTimer = undefined
  }
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  startGlobalLoading()
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    stopGlobalLoading()
    return response.data
  },
  (error) => {
    stopGlobalLoading()
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/console/login')
      ElMessage.warning('Session expired, please login again')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || 'Request failed')
    }
    return Promise.reject(error)
  }
)

export default request
