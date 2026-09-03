<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { getPayOrderPageApi, closePayOrderApi } from '@/api/pay/order'
import type { PageResult, PayOrderItem } from '@/api/pay/order/types'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { reactive, ref, unref } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({
  name: 'PayOrderList'
})

const router = useRouter()
const { push } = router

// 支付状态（对应后端 PayStatusEnum）
const payStatusOptions = [
  { value: 0, label: '待支付' },
  { value: 1, label: '支付中' },
  { value: 2, label: '支付成功' },
  { value: 3, label: '支付失败' },
  { value: 4, label: '已关闭' },
  { value: 5, label: '退款中' },
  { value: 6, label: '已退款' }
]

const statusTagType = (status: number) => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger' | 'primary'> = {
    0: 'warning', // 待支付
    1: 'primary', // 支付中
    2: 'success', // 支付成功
    3: 'danger', // 支付失败
    4: 'info', // 已关闭
    5: 'warning', // 退款中
    6: 'info' // 已退款
  }
  return map[status] || 'info'
}

const fmtAmount = (value?: number) =>
  value === null || value === undefined ? '-' : `¥${Number(value).toFixed(2)}`

const searchParams = ref({})
const setSearchParams = (params: any) => {
  searchParams.value = params
  getList()
}

const { tableRegister, tableState, tableMethods } = useTable({
  fetchDataApi: async () => {
    const { currentPage, pageSize } = tableState
    const s: any = unref(searchParams)
    const params: Record<string, any> = {
      page: unref(currentPage),
      pageSize: unref(pageSize)
    }
    if (s.paymentNo) params.paymentNo = s.paymentNo
    if (s.orderNo) params.orderNo = s.orderNo
    if (s.merchantNo) params.merchantNo = s.merchantNo
    if (s.status !== undefined && s.status !== null && s.status !== '') {
      params.status = s.status
    }
    // 日期范围拆成后端要求的 startTime/endTime
    if (Array.isArray(s.timeRange) && s.timeRange.length === 2) {
      params.startTime = s.timeRange[0]
      params.endTime = s.timeRange[1]
    }
    const res = await getPayOrderPageApi(params as any)
    const page = res.data as PageResult<PayOrderItem>
    return {
      list: page?.records || [],
      total: page?.total || 0
    }
  }
})
const { loading, dataList, total, currentPage, pageSize } = tableState
const { getList } = tableMethods

getList()

// 查看详情（详情页路由注册前给出提示，避免落到 404）
const goDetail = (row: PayOrderItem) => {
  const resolved = router.resolve(`/pay/order/${row.paymentNo}`)
  if (!resolved.matched.length) {
    ElMessage.info('订单详情页建设中')
    return
  }
  push(`/pay/order/${row.paymentNo}`)
}

const crudSchemas = reactive<CrudSchema[]>([
  {
    field: 'index',
    label: '序号',
    type: 'index',
    search: { hidden: true },
    form: { hidden: true },
    detail: { hidden: true },
    table: { width: 60, align: 'center' }
  },
  {
    field: 'paymentNo',
    label: '支付单号',
    minWidth: 180,
    search: {
      component: 'Input',
      componentProps: { placeholder: '支付单号', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'orderNo',
    label: '商户订单号',
    minWidth: 150,
    search: {
      component: 'Input',
      componentProps: { placeholder: '商户订单号', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'merchantNo',
    label: '商户号',
    minWidth: 150,
    search: {
      component: 'Input',
      componentProps: { placeholder: '商户号', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'merchantName',
    label: '商户名称',
    minWidth: 130,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'subject',
    label: '商品标题',
    minWidth: 140,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'amount',
    label: '支付金额',
    search: { hidden: true },
    table: {
      width: 120,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.amount)}</span>
      }
    }
  },
  {
    field: 'feeAmount',
    label: '手续费',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.feeAmount)}</span>
      }
    }
  },
  {
    field: 'status',
    label: '状态',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: payStatusOptions,
        clearable: true,
        placeholder: '状态'
      }
    },
    table: {
      width: 110,
      align: 'center',
      slots: {
        default: (data: any) => <ElTag type={statusTagType(data.status)}>{data.statusName}</ElTag>
      }
    }
  },
  {
    field: 'createTime',
    label: '创建时间',
    minWidth: 170,
    search: { hidden: true }
  },
  {
    field: 'timeRange',
    label: '创建时间',
    search: {
      component: 'DatePicker',
      componentProps: {
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        rangeSeparator: '至'
      }
    },
    form: { hidden: true },
    detail: { hidden: true },
    table: { hidden: true }
  },
  {
    field: 'action',
    label: '操作',
    search: { hidden: true },
    form: { hidden: true },
    detail: { hidden: true },
    table: {
      width: 150,
      align: 'center',
      slots: {
        default: (data: any) => {
          const row = data.row as PayOrderItem
          return (
            <>
              <BaseButton size="small" type="primary" link onClick={() => goDetail(row)}>
                详情
              </BaseButton>
              {row.status === 0 && (
                <BaseButton size="small" type="warning" link onClick={() => handleClose(row)}>
                  关单
                </BaseButton>
              )}
            </>
          )
        }
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)

// 手动关单（仅待支付显示入口，服务端条件更新保证幂等）
const handleClose = async (row: PayOrderItem) => {
  try {
    const { value } = await ElMessageBox.prompt(`确认关闭订单 ${row.paymentNo}？`, '手动关单', {
      confirmButtonText: '确认关单',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入关闭原因（选填）'
    })
    await closePayOrderApi({
      paymentNo: row.paymentNo,
      closeReason: value || '后台手动关闭'
    })
    ElMessage.success('关单成功')
    getList()
  } catch (e) {
    // 用户取消或接口报错（报错已由全局拦截器提示）
  }
}
</script>

<template>
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @reset="setSearchParams" @search="setSearchParams" />

    <Table
      v-model:page-size="pageSize"
      v-model:current-page="currentPage"
      :columns="allSchemas.tableColumns"
      :data="dataList"
      :loading="loading"
      :pagination="{ total }"
      @register="tableRegister"
    />
  </ContentWrap>
</template>
