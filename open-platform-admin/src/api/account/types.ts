/** 用户账户列表项（对应后端 AccountListVO） */
export interface AccountItem {
  /** 账户编号（UA 开头） */
  accountNo: string
  /** 用户编号（关联 auth_user 回填） */
  userNo: string
  /** 用户名 */
  username: string
  /** 手机号 */
  phone: string
  /** 账户余额（元） */
  balance: number
  /** 冻结金额（元） */
  frozenAmount: number
  /** 可用余额（balance - frozenAmount，元） */
  availableBalance: number
  /** 累计收入（元） */
  totalIncome: number
  /** 累计支出（元） */
  totalExpense: number
  /** 是否已实名认证 */
  realNameAuth: boolean
  /** 账户状态 code（1正常 0冻结） */
  status: number
  /** 账户状态名称 */
  statusName: string
  /** 创建时间（yyyy-MM-dd HH:mm:ss） */
  createTime: string
}

/** 用户账户分页查询参数 */
export interface AccountPageParams {
  page: number
  pageSize: number
  /** 账户号（模糊） */
  accountNo?: string
  /** 用户名（模糊） */
  username?: string
  /** 手机号（模糊） */
  phone?: string
}

/** 超管调账参数（amount 正数加款、负数扣款，不能为 0） */
export interface AdjustAccountParams {
  /** 账户类型 1-用户 2-商户 */
  accountType: 1 | 2
  /** 账户编号（UA/MA 开头） */
  accountNo: string
  /** 调账金额（正数加款、负数扣款） */
  amount: number
  /** 调账原因（必填，审计留痕，不超过 255 字） */
  remark: string
}
