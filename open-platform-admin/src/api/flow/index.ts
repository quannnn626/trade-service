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
