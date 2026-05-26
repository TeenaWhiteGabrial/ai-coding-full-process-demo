import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'

import { clearStoredSession, getStoredToken } from '@/utils/session'

interface RequestConfig extends InternalAxiosRequestConfig {
  showLoading?: boolean
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/ai-studio/v1',
  timeout: 30000,
})

let pendingCount = 0
let loadingTimer: number | undefined
let loadingInstance: LoadingInstance | null = null

function getBusinessCode(data: unknown) {
  if (!data || typeof data !== 'object' || !('code' in data)) {
    return null
  }

  const value = (data as { code?: unknown }).code
  const code = Number(value)
  return Number.isNaN(code) ? null : code
}

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

function redirectToLogin() {
  const currentPath = window.location.hash.replace(/^#/, '') || '/'

  if (currentPath.startsWith('/login')) return

  const redirectQuery = currentPath === '/'
    ? ''
    : `?redirect=${encodeURIComponent(currentPath)}`

  window.location.hash = `/login${redirectQuery}`
}

request.interceptors.request.use((config: RequestConfig) => {
  if (config.showLoading) {
    startGlobalLoading()
  }
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    if ((response.config as RequestConfig).showLoading) {
      stopGlobalLoading()
    }
    const data = response.data
    const businessCode = getBusinessCode(data)

    if (businessCode !== null && businessCode !== 200) {
      const message =
        (typeof data === 'object' && data && 'message' in data && String(data.message)) ||
        'Request failed'
      ElMessage.error(message)
      return Promise.reject(Object.assign(new Error(message), { response, data, businessCode }))
    }

    return data
  },
  (error) => {
    if ((error.config as RequestConfig | undefined)?.showLoading) {
      stopGlobalLoading()
    }
    if (error.response?.status === 401) {
      clearStoredSession()
      redirectToLogin()
      ElMessage.warning('Session expired, please login again')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || 'Request failed')
    }
    return Promise.reject(error)
  }
)

export default request
