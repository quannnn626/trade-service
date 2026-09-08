import request from '@/axios'
import type { RefundDetail, RefundItem, RefundPageParams } from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 退款订单分页列表（运营后台，支持按支付单号筛选，详情页复用）
 */
export const getRefundPageApi = (
  params: RefundPageParams
): Promise<IResponse<PageResult<RefundItem>>> => {
  return request.get({ url: '/api/refund/list', params })
}

/**
 * 退款订单详情（运营后台，无归属校验，可查任意商户退款单）
 */
export const getRefundDetailApi = (refundNo: string): Promise<IResponse<RefundDetail>> => {
  return request.get({ url: `/api/refund/${refundNo}` })
}
