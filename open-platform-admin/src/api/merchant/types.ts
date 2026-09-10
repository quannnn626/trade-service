/** 商户列表项（对应后端 MerchantListVO） */
export interface MerchantItem {
  /** 商户编号 */
  merchantNo: string
  /** 商户名称 */
  merchantName: string
  /** 企业全称 */
  companyName: string
  /** 联系人姓名 */
  contactName: string
  /** 联系人电话 */
  contactPhone: string
  /** 商户类型（值域后端未枚举，原值展示） */
  merchantType: number
  /** 结算费率（0.0060 = 0.6%） */
  settleFeeRate: number
  /** 状态 0禁用 1启用 */
  status: number
  /** 审核状态 0待审 1通过 2驳回 */
  auditStatus: number
  /** 创建时间 */
  createTime: string
}

/** 商户分页查询参数 */
export interface MerchantPageParams {
  page: number
  pageSize: number
  /** 商户名称（模糊） */
  merchantName?: string
  /** 状态 0禁用 1启用 */
  status?: number
  /** 审核状态 0待审 1通过 2驳回 */
  auditStatus?: number
}

/** 商户详情（对应后端 MerchantDetailVO，含资金账户） */
export interface MerchantDetail {
  /** 基本信息 */
  merchantNo: string
  merchantName: string
  companyName: string
  /** 营业执照号 */
  businessLicense: string
  /** 商户类型（值域后端未枚举，原值展示） */
  merchantType: number
  appKey: string
  /** 状态 0禁用 1启用 */
  status: number
  /** 审核状态 0待审 1通过 2驳回 */
  auditStatus: number
  /** 审核备注（驳回原因） */
  auditRemark: string | null
  /** 联系人 */
  contactName: string
  contactPhone: string
  contactEmail: string
  /** 结算配置 1-T+1 2-T+0 3-周结 4-月结 */
  settleType: number
  /** 结算费率（0.0060 = 0.6%） */
  settleFeeRate: number
  /** 风控配置 */
  dailyLimit: number
  singleLimit: number
  /** IP 白名单（JSON 数组字符串） */
  whiteIpList: string | null
  notifyUrl: string | null
  /** 资金账户 */
  accountNo: string
  balance: number
  frozenAmount: number
  /** 时间 */
  createTime: string
  updateTime: string
  /** 其他 */
  remark: string | null
  /** 密钥版本号 */
  secretVersion: number
}

/** 密钥轮换返回（appSecret 仅此一次返回，注意保存） */
export interface MerchantSecret {
  merchantNo: string
  appSecret: string
  secretVersion: number
  tip: string
}
