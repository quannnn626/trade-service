<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { auditMerchantApi, getMerchantPageApi } from '@/api/merchant'
import type { MerchantItem } from '@/api/merchant/types'
import type { PageResult } from '@/api/pay/order/types'
import { fmtRate } from '../common'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reactive, ref, unref } from 'vue'
import { useRouter } from 'vue-router'

const { push } = useRouter()

defineOptions({
  name: 'MerchantAudit'
})

const searchParams = ref({})
const setSearchParams = (params: any) => {
  searchParams.value = params
  getList()
}

// 待审列表：auditStatus 固定 0，只按商户名称筛选
const { tableRegister, tableState, tableMethods } = useTable({
  fetchDataApi: async () => {
    const { currentPage, pageSize } = tableState
    const s: any = unref(searchParams)
    const params: Record<string, any> = {
      page: unref(currentPage),
      pageSize: unref(pageSize),
      auditStatus: 0
    }
    if (s.merchantName) params.merchantName = s.merchantName
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
    field: 'createTime',
    label: '申请时间',
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
      width: 170,
      align: 'center',
      slots: {
        default: (data: any) => {
          const row = data.row as MerchantItem
          return (
            <>
              <BaseButton
                size="small"
                type="primary"
                link
                onClick={() => push(`/merchant/detail/${row.merchantNo}`)}
              >
                详情
              </BaseButton>
              <BaseButton size="small" type="success" link onClick={() => handleAudit(row, 1)}>
                通过
              </BaseButton>
              <BaseButton size="small" type="danger" link onClick={() => handleAudit(row, 2)}>
                驳回
              </BaseButton>
            </>
          )
        }
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)

// 商户审核：通过=启用并创建资金账户，驳回=必填原因（服务端有防重复审核，直接提交即可）
const handleAudit = async (row: MerchantItem, result: 1 | 2) => {
  try {
    let auditRemark = ''
    if (result === 2) {
      const { value } = await ElMessageBox.prompt(
        `确认驳回商户 ${row.merchantName}？`,
        '商户驳回',
        {
          confirmButtonText: '确认驳回',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入驳回原因（必填）',
          inputValidator: (v: string) => (v && v.trim() ? true : '驳回原因不能为空')
        }
      )
      auditRemark = value.trim()
    } else {
      await ElMessageBox.confirm(
        `确认通过商户 ${row.merchantName}（${row.merchantNo}）？通过后商户启用并自动创建资金账户。`,
        '商户审核',
        {
          confirmButtonText: '确认通过',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }
    const res = await auditMerchantApi({
      merchantNo: row.merchantNo,
      auditStatus: result,
      auditRemark
    })
    if (result === 1) {
      const accountNo = res.data?.accountNo
      ElMessage.success(accountNo ? `审核通过，已创建资金账户 ${accountNo}` : '审核通过')
    } else {
      ElMessage.success('已驳回')
    }
    getList()
  } catch {
    // 用户取消或接口报错（报错已由全局拦截器提示）
  }
}
</script>

<template>
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @reset="setSearchParams" @search="setSearchParams" />

    <div class="audit-tip">
      仅显示待审核商户。通过后商户立即启用并自动创建资金账户；驳回须填写原因，商户保持禁用。
      审核前请先点「详情」核对营业执照、结算配置与回调地址。
    </div>

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
.audit-tip {
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}
</style>
