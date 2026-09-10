import request from '@/axios'
import type { FlowItem, FlowQueryParams } from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 全局流水分页列表（资金流水列表页）
 * 筛选：账户类型/流水类型/时间范围（不支持按账户号，按账户号用 list-by-account）
 */
export const getFlowListApi = (
  params: FlowQueryParams
): Promise<IResponse<PageResult<FlowItem>>> => {
  return request.get({ url: '/api/flow/list', params })
}

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
