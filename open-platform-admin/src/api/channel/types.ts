/** 支付渠道列表项（对应后端 ChannelListVO） */
export interface ChannelItem {
  /** 渠道 ID */
  id: number
  /** 渠道编码（BALANCE/ALIPAY/WECHAT） */
  channelCode: string
  /** 渠道名称 */
  channelName: string
  /** 状态 code（对应后端 ChannelStatusEnum：0停用 1启用） */
  status: number
  /** 状态名称 */
  statusName: string
  /** 创建时间 */
  createTime: string
}
