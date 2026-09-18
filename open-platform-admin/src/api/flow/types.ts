/** 资金流水项（对应后端 FlowVO） */
export interface FlowItem {
  /** 流水号 */
  flowNo: string
  /** 账户类型 code（1-用户 2-商户） */
  accountType: number
  /** 账户类型名称 */
  accountTypeName: string
  /** 账户编号（用户钱包/商户资金账户） */
  accountNo: string
  /** 关联支付单号 */
  paymentNo: string
  /** 流水类型 code（1支出 2收入 3手续费 4退款支出 5退款收入 6充值 7冻结 8解冻 9调整） */
  flowType: number
  /** 流水类型名称 */
  flowTypeName: string
  /** 变动金额（支出为负、收入为正，元） */
  amount: number
  /** 变更前余额 */
  beforeBalance: number
  /** 变更后余额 */
  afterBalance: number
  /** 备注 */
  remark: string
  /** 创建时间 */
  createTime: string
}

/** 日汇总明细（按流水类型分组） */
export interface DailySummaryItem {
  /** 流水类型 code（1支出 2收入 3手续费 4退款支出 5退款收入 6充值 7冻结 8解冻 9调整） */
  flowType: number
  /** 流水类型名称 */
  flowTypeName: string
  /** 该类型笔数 */
  count: number
  /** 该类型金额合计（含符号，支出为负） */
  amount: number
}

/** 日汇总报表（对应后端 DailySummaryVO） */
export interface DailySummaryResult {
  /** 汇总日期（yyyy-MM-dd） */
  date: string
  /** 支付成功笔数（订单表口径，按支付时间归日） */
  payCount: number
  /** 交易额合计 = Σ订单金额（用户实付，未扣手续费） */
  tradeAmount: number
  /** 手续费合计 = Σ订单手续费（手续费只记在订单表 fee_amount） */
  feeAmount: number
  /** 商户到账合计 = Σ结算金额（= 交易额 - 手续费） */
  settleAmount: number
  /** 退款成功笔数（退款单表口径，按退款完成时间归日） */
  refundCount: number
  /** 退款金额合计 = Σ实际退款金额 */
  refundAmount: number
  /** 按流水类型汇总明细（资金流水口径，无记录的类型不返回） */
  items: DailySummaryItem[]
}

/** 资金流水查询参数 */
export interface FlowQueryParams {
  page: number
  pageSize: number
  /** 流水类型（1支出 2收入 3手续费 4退款支出 5退款收入 6充值 7冻结 8解冻 9调整） */
  flowType?: number
  /** 开始时间（yyyy-MM-dd HH:mm:ss） */
  startTime?: string
  /** 结束时间（yyyy-MM-dd HH:mm:ss） */
  endTime?: string
  /** 账户类型（1用户 2商户；仅全局列表接口支持，按账户号查询时接口不含此参数） */
  accountType?: number
}
