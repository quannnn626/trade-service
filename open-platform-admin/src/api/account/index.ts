import request from '@/axios'
import type {
  AccountItem,
  AccountPageParams,
  AdjustAccountParams,
  MerchantAccountItem,
  MerchantAccountPageParams
} from './types'
import type { PageResult } from '@/api/pay/order/types'

/**
 * 用户账户分页列表（运营后台）
 * 筛选：账户号/用户名/手机号（均模糊）
 */
export const getAccountPageApi = (
  params: AccountPageParams
): Promise<IResponse<PageResult<AccountItem>>> => {
  return request.get({ url: '/api/account/list', params })
}

/**
 * 商户账户分页列表（运营后台）
 * 筛选：账户号/商户号/商户名称（均模糊）
 */
export const getMerchantAccountPageApi = (
  params: MerchantAccountPageParams
): Promise<IResponse<PageResult<MerchantAccountItem>>> => {
  return request.get({ url: '/api/account/merchant/list', params })
}

/** 启用账户（状态置为正常，仅冻结账户可启用） */
export const enableAccountApi = (accountNo: string): Promise<IResponse> => {
  return request.put({ url: `/api/account/${accountNo}/enable` })
}

/** 禁用账户（状态置为冻结，禁用后支付/充值被拒） */
export const disableAccountApi = (accountNo: string): Promise<IResponse> => {
  return request.put({ url: `/api/account/${accountNo}/disable` })
}

/**
 * 超管调账（accountType 1-用户 2-商户；amount 正数加款、负数扣款）
 * 注：当前管理接口仅要求登录，超管角色权限待角色体系建立后收紧
 */
export const adjustAccountApi = (data: AdjustAccountParams): Promise<IResponse> => {
  return request.post({ url: '/api/account/adjust', data })
}
