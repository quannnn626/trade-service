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
