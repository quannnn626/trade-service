/** 接口日志列表项（对应后端 ApiLogListVO） */
export interface ApiLogItem {
  /** 日志 ID */
  id: number
  /** 商户号 */
  merchantNo: string
  /** 商户名称（关联 pay_merchant 回填） */
  merchantName: string
  /** 接口名称（如 pay.create） */
  apiName: string
  /** 请求方式（GET/POST） */
  requestMethod: string
  /** 请求地址 */
  requestUrl: string
  /** 验签结果 code（0通过 1失败） */
  signResult: number
  /** 验签结果名称 */
  signResultName: string
  /** 耗时（毫秒） */
  costTime: number
  /** 请求参数 */
  requestParam: string
  /** 响应结果 */
  responseResult: string
  /** 错误信息 */
  errorMsg: string
  /** 调用时间 */
  createTime: string
}

/** 接口日志分页查询参数 */
export interface ApiLogPageParams {
  page: number
  pageSize: number
  /** 商户号（模糊） */
  merchantNo?: string
  /** 接口名（模糊） */
  apiName?: string
  /** 验签结果（0通过 1失败） */
  signResult?: number
  /** 调用时间起（yyyy-MM-dd HH:mm:ss） */
  startTime?: string
  /** 调用时间止（yyyy-MM-dd HH:mm:ss） */
  endTime?: string
}
