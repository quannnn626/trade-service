/** MyBatis-Plus 分页结果结构（后端 Page 直接序列化） */
export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 支付订单列表项（对应后端 PayOrderListVO） */
export interface PayOrderItem {
  /** 支付单号 */
  paymentNo: string
  /** 商户订单号 */
  orderNo: string
  /** 商户编号（关联回填） */
  merchantNo: string
  /** 商户名称（关联回填） */
  merchantName: string
  /** 商品标题 */
  subject: string
  /** 支付金额（元） */
  amount: number
  /** 手续费金额（元） */
  feeAmount: number
  /** 结算金额（元） */
  settleAmount: number
  /** 支付状态 code（0待支付 1支付中 2成功 3失败 4已关闭 5退款中 6已退款） */
  status: number
  /** 支付状态名称 */
  statusName: string
  /** 支付完成时间 */
  payTime: string
  /** 创建时间 */
  createTime: string
}

/** 支付订单详情（对应后端 PayOrderDetailVO，时间字段为 yyyy-MM-dd HH:mm:ss 字符串） */
export interface PayOrderDetail {
  /** 支付单号 */
  paymentNo: string
  /** 商户订单号 */
  orderNo: string
  /** 商城支付单号 */
  merchantPaymentNo: string
  /** 商户编号 */
  merchantNo: string
  /** 商户名称 */
  merchantName: string
  /** 付款用户 ID（未支付前为空） */
  userId: number | null
  /** 用户编号 */
  userNo: string | null
  /** 用户名 */
  userName: string | null
  /** 用户手机号 */
  userPhone: string | null
  /** 支付渠道编码 */
  channelCode: string | null
  /** 支付渠道名称 */
  channelName: string | null
  /** 商品标题 */
  subject: string
  /** 订单描述 */
  description: string | null
  /** 支付金额（元） */
  amount: number
  /** 手续费金额（元） */
  feeAmount: number
  /** 结算金额（元） */
  settleAmount: number
  /** 支付状态 code */
  status: number
  /** 支付状态名称 */
  statusName: string
  /** 客户端/服务器 IP */
  clientIp: string | null
  /** 订单级回调地址 */
  notifyUrl: string | null
  /** 支付完成跳转地址 */
  returnUrl: string | null
  /** 附加数据（透传） */
  attach: string | null
  /** 支付过期时间 */
  expireTime: string | null
  /** 订单超时自动关闭时间 */
  timeoutExpire: string | null
  /** 支付完成时间 */
  payTime: string | null
  /** 关单时间 */
  closeTime: string | null
  /** 关单原因 */
  closeReason: string | null
  /** 结算状态 0-未结算 1-已结算 */
  settleStatus: number | null
  /** 结算时间 */
  settleTime: string | null
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/** 支付订单分页查询参数 */
export interface PayOrderPageParams {
  page: number
  pageSize: number
  /** 支付单号（模糊） */
  paymentNo?: string
  /** 商户订单号（模糊） */
  orderNo?: string
  /** 商户号（模糊） */
  merchantNo?: string
  /** 支付状态（精确） */
  status?: number
  /** 创建时间起（yyyy-MM-dd HH:mm:ss） */
  startTime?: string
  /** 创建时间止（yyyy-MM-dd HH:mm:ss） */
  endTime?: string
}
