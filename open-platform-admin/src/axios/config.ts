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
  }
  if (response.data.code === SUCCESS_CODE) {
    return response.data
  }
  if (response.data.code === 401) {
    // 登录态过期：静默刷新 token 并重试；刷新失败时内部会登出，不在此弹错
    return refreshAndRetry(response)
  }
  ElMessage.error(response?.data?.message)
  return response.data
}

// 刷新请求单飞：并发 401 只发一次刷新，其余请求排队拿同一个新 token
let refreshingPromise: Promise<string> | null = null

const refreshAccessToken = (): Promise<string> => {
  const userStore = useUserStoreWithOut()
  if (!refreshingPromise) {
    refreshingPromise = axios
      .post('/api/auth/refresh', {
        userId: userStore.getUserId,
        refreshToken: userStore.getRefreshToken
      })
      .then((res) => {
        // 后端约定：错误同样返回 HTTP 200 + 业务码，必须检查，否则 null 会被当有效 token
        const body = res.data
        if (!body || body.code !== SUCCESS_CODE || !body.data) {
          throw new Error(body?.message || '刷新登录状态失败')
        }
        userStore.setToken(body.data)
        return body.data
      })
      .catch((err) => {
        // 刷新失败（refreshToken 在服务端已失效）：清登录态回登录页
        ElMessage.error(err?.message || '登录已过期，请重新登录')
        userStore.reset()
        throw err
      })
      .finally(() => {
        refreshingPromise = null
      })
  }
  return refreshingPromise
}

const refreshAndRetry = async (response: AxiosResponse) => {
  try {
    await refreshAccessToken()
    // 用新 token 重试原请求（裸 axios，避免递归走拦截器）
    const cfg = { ...response.config }
    delete cfg.signal
    cfg.headers.set('Authorization', useUserStoreWithOut().getToken || '')
    const retryRes = await axios.request(cfg)
    const body = retryRes.data
    if (body.code === SUCCESS_CODE) {
      return body
    }
    // 新 token 依旧 401（如用户被禁用）：不再无限重试，登出
    if (body.code === 401) {
      ElMessage.error(body.message || '登录已过期，请重新登录')
      useUserStoreWithOut().reset()
      return body
    }
    ElMessage.error(body?.message)
    return body
  } catch {
    // 刷新失败已登出，返回错误结果避免业务层无响应
    return { code: 401, message: '登录已过期，请重新登录', data: null }
  }
}

export { defaultResponseInterceptors, defaultRequestInterceptors }
