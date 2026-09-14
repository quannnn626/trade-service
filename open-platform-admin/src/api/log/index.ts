import request from '@/axios'
import type { ApiLogItem, ApiLogPageParams } from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 接口日志分页列表（运营后台）
 * 筛选：商户号/接口名（模糊）、验签结果、调用时间范围
 */
export const getApiLogPageApi = (
  params: ApiLogPageParams
): Promise<IResponse<PageResult<ApiLogItem>>> => {
  return request.get({ url: '/api/log/list', params })
}
