/** 回调通知记录项（对应后端 NotifyListVO） */
export interface NotifyItem {
  /** 通知记录 ID（手动重试用） */
  id: number
  /** 支付单号 */
  paymentNo: string
  /** 商户号 */
  merchantNo: string
  /** 商户名称 */
  merchantName: string
  /** 通知地址 */
  notifyUrl: string
  /** 通知类型 code（1支付成功 2退款成功 3退款失败） */
  notifyType: number
  /** 通知类型名称 */
  notifyTypeName: string
  /** 通知状态 code（0待通知 1成功 2失败达上限） */
  notifyStatus: number
  /** 通知状态名称 */
  notifyStatusName: string
  /** 已重试次数 */
  retryCount: number
  /** 最大重试次数 */
  maxRetry: number
  /** 下次重试时间（成功或达上限后为 null） */
  nextRetryTime: string | null
  /** 最后一次错误信息 */
  lastError: string | null
  /** 请求参数 */
  requestData: string | null
  /** 商户响应结果 */
  responseData: string | null
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/** 回调通知查询参数 */
export interface NotifyQueryParams {
  page: number
  pageSize: number
  /** 支付单号 */
  paymentNo?: string
  /** 商户号 */
  merchantNo?: string
  /** 通知类型 */
  notifyType?: number
  /** 通知状态 */
  notifyStatus?: number
}
