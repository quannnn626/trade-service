import request from '@/axios'
import type { MerchantItem, MerchantPageParams } from './types'
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
