import request from '@/axios'
import type { ChannelItem } from './types'

/**
 * 支付渠道列表（运营后台）
 * 当前仅预置 BALANCE/ALIPAY/WECHAT 三条，接口返回普通数组、无分页
 */
export const getChannelListApi = (): Promise<IResponse<ChannelItem[]>> => {
  return request.get({ url: '/api/channel/list' })
}

/**
 * 启用支付渠道（后端幂等：已是启用状态直接返回成功）
 */
export const enableChannelApi = (channelCode: string): Promise<IResponse> => {
  return request.put({ url: `/api/channel/${channelCode}/enable` })
}

/**
 * 停用支付渠道（后端幂等；停用后商户用该渠道创建支付订单会被拒绝）
 */
export const disableChannelApi = (channelCode: string): Promise<IResponse> => {
  return request.put({ url: `/api/channel/${channelCode}/disable` })
}
