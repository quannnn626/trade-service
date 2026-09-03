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
