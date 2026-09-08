<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { getRefundPageApi } from '@/api/refund'
import type { RefundItem } from '@/api/refund/types'
import type { PageResult } from '@/api/pay/order/types'
import { fmtAmount, refundAuditTagType, refundStatusTagType } from '../common'
import { ElTag } from 'element-plus'
import { reactive, ref, unref } from 'vue'
import { useRouter } from 'vue-router'

const { push } = useRouter()

defineOptions({
  name: 'PayRefundList'
})

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
    if (s.refundNo) params.refundNo = s.refundNo
    if (s.paymentNo) params.paymentNo = s.paymentNo
    if (s.merchantNo) params.merchantNo = s.merchantNo
    if (s.status !== undefined && s.status !== null && s.status !== '') {
      params.status = s.status
    }
    if (s.auditStatus !== undefined && s.auditStatus !== null && s.auditStatus !== '') {
      params.auditStatus = s.auditStatus
    }
    // 日期范围拆成后端要求的 startTime/endTime
    if (Array.isArray(s.timeRange) && s.timeRange.length === 2) {
      params.startTime = s.timeRange[0]
      params.endTime = s.timeRange[1]
    }
    const res = await getRefundPageApi(params as any)
    const page = res.data as PageResult<RefundItem>
    return {
      list: page?.records || [],
      total: page?.total || 0
    }
  }
})
const { loading, dataList, total, currentPage, pageSize } = tableState
const { getList } = tableMethods

getList()

// 退款状态筛选（对应 RefundStatusEnum：0处理中 1成功 2失败）
const refundStatusOptions = [
  { value: 0, label: '处理中' },
  { value: 1, label: '成功' },
  { value: 2, label: '失败' }
]

// 审核状态筛选（对应 RefundAuditStatusEnum：0待审核 1通过 2驳回）
const auditStatusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '通过' },
  { value: 2, label: '驳回' }
]

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
    field: 'refundNo',
    label: '退款单号',
    minWidth: 180,
    search: {
      component: 'Input',
      componentProps: { placeholder: '退款单号', clearable: true }
    },
    table: { showOverflowTooltip: true }
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
    field: 'applyAmount',
    label: '申请金额',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.applyAmount)}</span>
      }
    }
  },
  {
    field: 'actualAmount',
    label: '实退金额',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.actualAmount)}</span>
      }
    }
  },
  {
    field: 'feeRefund',
    label: '退手续费',
    search: { hidden: true },
    table: {
      width: 100,
      align: 'right',
      slots: {
        default: (data: any) => {
          // 手续费为 0 时显示 -，避免一行 ¥0.00 占重点
          const v = data.feeRefund as number
          return <span>{v ? fmtAmount(v) : '-'}</span>
        }
      }
    }
  },
  {
    field: 'refundTypeName',
    label: '退款类型',
    minWidth: 100,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'status',
    label: '退款状态',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: refundStatusOptions,
        clearable: true,
        placeholder: '退款状态'
      }
    },
    table: {
      width: 110,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag type={refundStatusTagType(data.status)}>{data.statusName}</ElTag>
        )
      }
    }
  },
  {
    field: 'auditStatus',
    label: '审核状态',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: auditStatusOptions,
        clearable: true,
        placeholder: '审核状态'
      }
    },
    table: {
      width: 110,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag type={refundAuditTagType(data.auditStatus)}>{data.auditStatusName}</ElTag>
        )
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
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) => {
          const row = data.row as RefundItem
          return (
            <BaseButton
              size="small"
              type="primary"
              link
              onClick={() => push(`/pay/refund/${row.refundNo}`)}
            >
              详情
            </BaseButton>
          )
        }
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)
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
