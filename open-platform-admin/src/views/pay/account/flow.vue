<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { getFlowListApi, getFlowListByAccountApi } from '@/api/flow'
import type { FlowItem } from '@/api/flow/types'
import type { PageResult } from '@/api/pay/order/types'
import {
  accountTypeOptions,
  amountColorClass,
  flowTypeOptions,
  fmtAmount,
  fmtSignedAmount,
  fmtTime
} from '../common'
import { reactive, ref, unref } from 'vue'

defineOptions({
  name: 'AccountFlowList'
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
    if (s.flowType !== undefined && s.flowType !== null && s.flowType !== '') {
      params.flowType = s.flowType
    }
    // 日期范围拆成后端要求的 startTime/endTime
    if (Array.isArray(s.timeRange) && s.timeRange.length === 2) {
      params.startTime = s.timeRange[0]
      params.endTime = s.timeRange[1]
    }
    // 填了账户号走按账户查询（后端按 UA/MA 前缀识别账户类型，此时账户类型筛选不适用）
    const accountNo = s.accountNo ? String(s.accountNo).trim() : ''
    const res = accountNo
      ? await getFlowListByAccountApi(accountNo, params as any)
      : await getFlowListApi({
          ...params,
          ...(s.accountType !== undefined && s.accountType !== null && s.accountType !== ''
            ? { accountType: s.accountType }
            : {})
        } as any)
    const page = res.data as PageResult<FlowItem>
    return {
      list: page?.records || [],
      total: page?.total || 0
    }
  }
})
const { loading, dataList, total, currentPage, pageSize } = tableState
const { getList } = tableMethods

getList()

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
    field: 'flowNo',
    label: '流水号',
    minWidth: 180,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'accountNo',
    label: '账户编号',
    minWidth: 170,
    search: {
      component: 'Input',
      componentProps: { placeholder: '账户号（UA/MA 开头）', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'accountType',
    label: '账户类型',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: accountTypeOptions,
        clearable: true,
        placeholder: '账户类型'
      }
    },
    table: {
      width: 100,
      align: 'center',
      slots: {
        // 表格展示后端回填的名称（数值 code 仅用于筛选）
        default: (data: any) => <span>{data.accountTypeName}</span>
      }
    }
  },
  {
    field: 'paymentNo',
    label: '关联支付单号',
    minWidth: 180,
    search: { hidden: true },
    table: {
      showOverflowTooltip: true,
      slots: {
        default: (data: any) => <span>{data.paymentNo || '-'}</span>
      }
    }
  },
  {
    field: 'flowType',
    label: '流水类型',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: flowTypeOptions,
        clearable: true,
        placeholder: '流水类型'
      }
    },
    table: {
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) => <span>{data.flowTypeName}</span>
      }
    }
  },
  {
    field: 'amount',
    label: '变动金额',
    search: { hidden: true },
    table: {
      width: 120,
      align: 'right',
      slots: {
        default: (data: any) => (
          <span class={amountColorClass(data.amount)}>{fmtSignedAmount(data.amount)}</span>
        )
      }
    }
  },
  {
    field: 'beforeBalance',
    label: '余额变化',
    search: { hidden: true },
    table: {
      width: 180,
      align: 'right',
      slots: {
        default: (data: any) => (
          <span>
            {fmtAmount(data.beforeBalance)} → {fmtAmount(data.afterBalance)}
          </span>
        )
      }
    }
  },
  {
    field: 'remark',
    label: '备注',
    minWidth: 140,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'createTime',
    label: '创建时间',
    width: 170,
    search: { hidden: true },
    table: {
      slots: {
        default: (data: any) => <span>{fmtTime(data.createTime)}</span>
      }
    }
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

<style lang="less" scoped>
.amount-in {
  color: var(--el-color-success);
}

.amount-out {
  color: var(--el-color-danger);
}
</style>
