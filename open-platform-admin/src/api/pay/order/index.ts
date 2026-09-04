import request from '@/axios'
import type { PageResult, PayOrderDetail, PayOrderItem, PayOrderPageParams } from './types'

/**
 * 支付订单详情（运营后台，无归属校验，可查任意商户订单）
 */
export const getPayOrderDetailApi = (paymentNo: string): Promise<IResponse<PayOrderDetail>> => {
  return request.get({ url: `/api/pay/order/${paymentNo}` })
}

/**
 * 支付订单分页列表（运营后台）
 */
export const getPayOrderPageApi = (
  params: PayOrderPageParams
): Promise<IResponse<PageResult<PayOrderItem>>> => {
  return request.get({ url: '/api/pay/order/list', params })
}

/**
 * 手动关单（仅待支付状态可关，条件更新保证幂等）
 */
export const closePayOrderApi = (data: {
  paymentNo: string
  closeReason: string
}): Promise<IResponse> => {
  return request.post({ url: '/api/pay/order/close', data })
}
