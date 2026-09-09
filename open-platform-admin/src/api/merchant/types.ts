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
