import request from '@/axios'
import type { RefundItem, RefundPageParams } from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 退款订单分页列表（运营后台，支持按支付单号筛选，详情页复用）
 */
export const getRefundPageApi = (
  params: RefundPageParams
): Promise<IResponse<PageResult<RefundItem>>> => {
  return request.get({ url: '/api/refund/list', params })
}
