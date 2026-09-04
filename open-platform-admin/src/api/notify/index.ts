import request from '@/axios'
import type { NotifyItem, NotifyQueryParams } from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 回调通知分页列表（运营后台）
 */
export const getNotifyPageApi = (
  params: NotifyQueryParams
): Promise<IResponse<PageResult<NotifyItem>>> => {
  return request.get({ url: '/api/notify/list', params })
}

/**
 * 手动重试回调通知
 */
export const retryNotifyApi = (notifyId: number): Promise<IResponse> => {
  return request.post({ url: '/api/notify/retry', params: { notifyId } })
}
