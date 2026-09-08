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

/**
 * 退款审核（auditResult 1-通过 2-驳回；驳回时 auditRemark 必填，作为失败原因）
 */
export const auditRefundApi = (data: {
  refundNo: string
  auditResult: 1 | 2
  auditRemark?: string
}): Promise<IResponse> => {
  return request.post({ url: '/api/refund/audit', data })
}
