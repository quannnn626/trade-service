/** 退款订单列表项（对应后端 RefundListVO） */
export interface RefundItem {
  /** 退款单号 */
  refundNo: string
  /** 支付单号 */
  paymentNo: string
  /** 商户退款单号 */
  merchantRefundNo: string
  /** 商户编号 */
  merchantNo: string
  /** 商户名称 */
  merchantName: string
  /** 申请退款金额（元） */
  applyAmount: number
  /** 实际退款金额（元） */
  actualAmount: number
  /** 退还手续费（元） */
  feeRefund: number
  /** 退款类型 code（1-全额 2-部分） */
  refundType: number
  /** 退款类型名称 */
  refundTypeName: string
  /** 退款状态 code（0处理中 1成功 2失败） */
  status: number
  /** 退款状态名称 */
  statusName: string
  /** 审核状态 code（0-待审核 1-通过 2-驳回） */
  auditStatus: number
  /** 审核状态名称 */
  auditStatusName: string
  /** 退款原因 */
  refundReason: string | null
  /** 完成时间 */
  finishTime: string | null
  /** 创建时间 */
  createTime: string
}

/** 退款订单分页查询参数 */
export interface RefundPageParams {
  page: number
  pageSize: number
  /** 退款单号 */
  refundNo?: string
  /** 支付单号 */
  paymentNo?: string
  /** 商户号 */
  merchantNo?: string
  /** 退款状态 */
  status?: number
  /** 审核状态 */
  auditStatus?: number
  /** 开始时间（yyyy-MM-dd HH:mm:ss） */
  startTime?: string
  /** 结束时间（yyyy-MM-dd HH:mm:ss） */
  endTime?: string
}
