import request from '@/axios'
import type { FlowItem, FlowQueryParams } from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 按支付单号分页查询流水（订单详情页用）
 */
export const getFlowListByPaymentApi = (
  paymentNo: string,
  params: FlowQueryParams
): Promise<IResponse<PageResult<FlowItem>>> => {
  return request.get({ url: `/api/flow/list-by-payment/${paymentNo}`, params })
}

/**
 * 按账户编号分页查询流水（用户钱包 UA / 商户账户 MA）
 */
export const getFlowListByAccountApi = (
  accountNo: string,
  params: FlowQueryParams
): Promise<IResponse<PageResult<FlowItem>>> => {
  return request.get({ url: `/api/flow/list-by-account/${accountNo}`, params })
}
