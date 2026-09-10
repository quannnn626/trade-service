import request from '@/axios'
import type {
  MerchantAuditParams,
  MerchantAuditResult,
  MerchantDetail,
  MerchantItem,
  MerchantPageParams,
  MerchantSecret
} from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 商户分页列表（运营后台）
 * 筛选：商户名称（模糊）/状态/审核状态
 */
export const getMerchantPageApi = (
  params: MerchantPageParams
): Promise<IResponse<PageResult<MerchantItem>>> => {
  return request.get({ url: '/api/merchant/list', params })
}

/**
 * 商户审核（通过=启用商户+创建资金账户；驳回=须填原因，商户保持禁用）
 * 服务端已防重复审核，待审列表天然只含未审核商户
 */
export const auditMerchantApi = (
  data: MerchantAuditParams
): Promise<IResponse<MerchantAuditResult>> => {
  return request.post({ url: '/api/merchant/audit', data })
}

/** 商户详情（含资金账户、密钥版本） */
export const getMerchantDetailApi = (merchantNo: string): Promise<IResponse<MerchantDetail>> => {
  return request.get({ url: `/api/merchant/${merchantNo}` })
}

/** 启用商户 */
export const enableMerchantApi = (merchantNo: string): Promise<IResponse> => {
  return request.put({ url: `/api/merchant/${merchantNo}/enable` })
}

/** 禁用商户（禁用后无法发起支付） */
export const disableMerchantApi = (merchantNo: string): Promise<IResponse> => {
  return request.put({ url: `/api/merchant/${merchantNo}/disable` })
}

/** 密钥轮换（旧密钥立即失效，新 appSecret 仅返回一次） */
export const rotateMerchantSecretApi = (merchantNo: string): Promise<IResponse<MerchantSecret>> => {
  return request.put({ url: `/api/merchant/${merchantNo}/rotate-secret` })
}
