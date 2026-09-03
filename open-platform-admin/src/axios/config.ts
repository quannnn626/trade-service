import { AxiosResponse, InternalAxiosRequestConfig } from './types'
import { ElMessage } from 'element-plus'
import qs from 'qs'
import { SUCCESS_CODE, TRANSFORM_REQUEST_DATA } from '@/constants'
import { useUserStoreWithOut } from '@/store/modules/user'
import { objToFormData } from '@/utils'
import axios from 'axios'

const defaultRequestInterceptors = (config: InternalAxiosRequestConfig) => {
  if (
    config.method === 'post' &&
    config.headers['Content-Type'] === 'application/x-www-form-urlencoded'
  ) {
    config.data = qs.stringify(config.data)
  } else if (
    TRANSFORM_REQUEST_DATA &&
    config.method === 'post' &&
    config.headers['Content-Type'] === 'multipart/form-data' &&
    !(config.data instanceof FormData)
  ) {
    config.data = objToFormData(config.data)
  }
  if (config.method === 'get' && config.params) {
    let url = config.url as string
    url += '?'
    const keys = Object.keys(config.params)
    for (const key of keys) {
      if (config.params[key] !== void 0 && config.params[key] !== null) {
        url += `${key}=${encodeURIComponent(config.params[key])}&`
      }
    }
    url = url.substring(0, url.length - 1)
    config.params = {}
    config.url = url
  }
  return config
}

const defaultResponseInterceptors = (response: AxiosResponse) => {
  if (response?.config?.responseType === 'blob') {
    // 如果是文件流，直接过
    return response
  } else if (response.data.code === SUCCESS_CODE) {
    return response.data
  } else {
    ElMessage.error(response?.data?.message)
    if (response?.data?.code === 401) {
      const userStore = useUserStoreWithOut()
      return refreshAndRetry(response, userStore)
    }
  }
}

let isRefreshing = false

const refreshAndRetry = async (
  response: AxiosResponse,
  userStore: ReturnType<typeof useUserStoreWithOut>
) => {
  isRefreshing = true
  try {
    const res = await axios.post('/api/auth/refresh', {
      userId: userStore.getUserId,
      refreshToken: userStore.getRefreshToken
    })
    const newToken = res.data.data as string
    userStore.setToken(newToken)
    // 重试原请求
    response.config.headers['Authorization'] = newToken
    const retryRes = await axios.request(response.config)
    // 由上层拦截器处理 — 重新走一遍当前拦截器逻辑
    return defaultResponseInterceptors(retryRes)
  } catch {
    userStore.logout()
  } finally {
    isRefreshing = false
  }
}

export { defaultResponseInterceptors, defaultRequestInterceptors }
