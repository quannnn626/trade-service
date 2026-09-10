/**
 * 支付模块公共展示工具（金额格式化、状态标签映射）
 */

/** 金额格式化：元 → ¥xx.xx（空值显示 -） */
export const fmtAmount = (value?: number | null) =>
  value === null || value === undefined ? '-' : `¥${Number(value).toFixed(2)}`

/** 时间格式化：去掉 ISO 的 T 与毫秒/时区尾巴，与库内 datetime 原值一致（不做时区换算） */
export const fmtTime = (value?: string | null) =>
  !value ? '-' : value.replace('T', ' ').slice(0, 19)

/** 支付状态筛选选项（对应后端 PayStatusEnum code） */
export const payStatusOptions = [
  { value: 0, label: '待支付' },
  { value: 1, label: '支付中' },
  { value: 2, label: '支付成功' },
  { value: 3, label: '支付失败' },
  { value: 4, label: '已关闭' },
  { value: 5, label: '退款中' },
  { value: 6, label: '已退款' }
]

/** 支付状态标签颜色 */
export const statusTagType = (status?: number) => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger' | 'primary'> = {
    0: 'warning', // 待支付
    1: 'primary', // 支付中
    2: 'success', // 支付成功
    3: 'danger', // 支付失败
    4: 'info', // 已关闭
    5: 'warning', // 退款中
    6: 'info' // 已退款
  }
  return (status !== undefined ? map[status] : undefined) || 'info'
}

/** 回调通知状态标签颜色（对应 NotifyStatusEnum：0待通知 1成功 2失败达上限） */
export const notifyStatusTagType = (status: number) => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger'> = {
    0: 'warning', // 待通知
    1: 'success', // 通知成功
    2: 'danger' // 失败达上限
  }
  return map[status] || 'info'
}

/** 退款状态标签颜色（对应 RefundStatusEnum：0处理中 1成功 2失败） */
export const refundStatusTagType = (status?: number) => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger'> = {
    0: 'warning', // 处理中
    1: 'success', // 成功
    2: 'danger' // 失败
  }
  return (status !== undefined ? map[status] : undefined) || 'info'
}

/** 退款审核状态标签颜色（对应 RefundAuditStatusEnum：0待审核 1通过 2驳回） */
export const refundAuditTagType = (status?: number) => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger'> = {
    0: 'warning', // 待审核
    1: 'success', // 通过
    2: 'danger' // 驳回
  }
  return (status !== undefined ? map[status] : undefined) || 'info'
}

/** 流水金额显示：收入带 + 号、支出为负（空值显示 -） */
export const fmtSignedAmount = (value?: number | null) =>
  value === null || value === undefined ? '-' : (value > 0 ? '+' : '') + fmtAmount(value)

/** 流水金额颜色类：支出红 / 收入绿（配合样式 .amount-out/.amount-in 使用） */
export const amountColorClass = (value?: number | null) =>
  value !== null && value !== undefined && value < 0 ? 'amount-out' : 'amount-in'

/** 费率百分比显示：0.0060 → 0.6%（空值显示 -） */
export const fmtRate = (value?: number | null) =>
  value === null || value === undefined ? '-' : `${Number((Number(value) * 100).toFixed(4))}%`

/** 资金流水类型筛选选项（对应 AccountFlowTypeEnum 1-9） */
export const flowTypeOptions = [
  { value: 1, label: '支出' },
  { value: 2, label: '收入' },
  { value: 3, label: '手续费' },
  { value: 4, label: '退款支出' },
  { value: 5, label: '退款收入' },
  { value: 6, label: '充值' },
  { value: 7, label: '冻结' },
  { value: 8, label: '解冻' },
  { value: 9, label: '调整' }
]

/** 资金账户类型选项（1-用户 2-商户） */
export const accountTypeOptions = [
  { value: 1, label: '用户账户' },
  { value: 2, label: '商户账户' }
]

/** 商户状态标签颜色（对应 MerchantStatusEnum：0禁用 1启用） */
export const merchantStatusTagType = (status?: number) => {
  const map: Record<number, 'success' | 'info' | 'warning'> = {
    0: 'warning', // 禁用
    1: 'success' // 启用
  }
  return (status !== undefined ? map[status] : undefined) || 'info'
}

/** 商户审核状态标签颜色（0待审核 1通过 2驳回） */
export const merchantAuditTagType = (status?: number) => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger'> = {
    0: 'warning', // 待审核
    1: 'success', // 通过
    2: 'danger' // 驳回
  }
  return (status !== undefined ? map[status] : undefined) || 'info'
}
