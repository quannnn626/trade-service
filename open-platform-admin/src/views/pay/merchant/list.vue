<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { getMerchantPageApi } from '@/api/merchant'
import type { MerchantItem } from '@/api/merchant/types'
import type { PageResult } from '@/api/pay/order/types'
import {
  fmtRate,
  merchantAuditStatusText,
  merchantAuditTagType,
  merchantStatusTagType
} from '../common'
import { ElTag } from 'element-plus'
import { reactive, ref, unref } from 'vue'
import { useRouter } from 'vue-router'

const { push } = useRouter()

defineOptions({
  name: 'MerchantList'
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
    if (s.merchantName) params.merchantName = s.merchantName
    if (s.status !== undefined && s.status !== null && s.status !== '') {
      params.status = s.status
    }
    if (s.auditStatus !== undefined && s.auditStatus !== null && s.auditStatus !== '') {
      params.auditStatus = s.auditStatus
    }
    const res = await getMerchantPageApi(params as any)
    const page = res.data as PageResult<MerchantItem>
    return {
      list: page?.records || [],
      total: page?.total || 0
    }
  }
})
const { loading, dataList, total, currentPage, pageSize } = tableState
const { getList } = tableMethods

getList()

// 启用状态筛选（0禁用 1启用）
const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

// 审核状态筛选（0待审 1通过 2驳回）
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
    field: 'merchantNo',
    label: '商户号',
    minWidth: 170,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'merchantName',
    label: '商户名称',
    minWidth: 130,
    search: {
      component: 'Input',
      componentProps: { placeholder: '商户名称', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'companyName',
    label: '企业全称',
    minWidth: 170,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'contactName',
    label: '联系人',
    width: 100,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'contactPhone',
    label: '联系电话',
    width: 130,
    search: { hidden: true }
  },
  {
    field: 'merchantType',
    label: '商户类型',
    width: 100,
    search: { hidden: true }
  },
  {
    field: 'settleFeeRate',
    label: '结算费率',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtRate(data.settleFeeRate)}</span>
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
        options: statusOptions,
        clearable: true,
        placeholder: '状态'
      }
    },
    table: {
      width: 90,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag type={merchantStatusTagType(data.status)} size="small">
            {data.status === 1 ? '启用' : '禁用'}
          </ElTag>
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
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag type={merchantAuditTagType(data.auditStatus)} size="small">
            {merchantAuditStatusText(data.auditStatus)}
          </ElTag>
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
    field: 'action',
    label: '操作',
    search: { hidden: true },
    form: { hidden: true },
    detail: { hidden: true },
    table: {
      width: 90,
      align: 'center',
      slots: {
        default: (data: any) => {
          const row = data.row as MerchantItem
          return (
            <BaseButton
              size="small"
              type="primary"
              link
              onClick={() => push(`/merchant/detail/${row.merchantNo}`)}
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
