<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { Dialog } from '@/components/Dialog'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { adjustAccountApi, getMerchantAccountPageApi } from '@/api/account'
import { getFlowListByAccountApi } from '@/api/flow'
import type { MerchantAccountItem } from '@/api/account/types'
import type { FlowItem } from '@/api/flow/types'
import type { PageResult } from '@/api/pay/order/types'
import { amountColorClass, fmtAmount, fmtSignedAmount } from '../common'
import {
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElPagination,
  ElRadio,
  ElRadioGroup,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus'
import type { FormInstance } from 'element-plus'
import { reactive, ref, unref } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({
  name: 'MerchantAccountList'
})

const { push } = useRouter()

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
    if (s.accountNo) params.accountNo = s.accountNo
    if (s.merchantNo) params.merchantNo = s.merchantNo
    if (s.merchantName) params.merchantName = s.merchantName
    const res = await getMerchantAccountPageApi(params as any)
    const page = res.data as PageResult<MerchantAccountItem>
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
    minWidth: 180,
    search: {
      component: 'Input',
      componentProps: { placeholder: '商户号', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'merchantName',
    label: '商户名称',
    minWidth: 140,
    search: {
      component: 'Input',
      componentProps: { placeholder: '商户名称', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'accountNo',
    label: '账户号',
    minWidth: 180,
    search: {
      component: 'Input',
      componentProps: { placeholder: '账户号（MA 开头）', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'balance',
    label: '账户余额',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.balance)}</span>
      }
    }
  },
  {
    field: 'frozenAmount',
    label: '冻结金额',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.frozenAmount)}</span>
      }
    }
  },
  {
    field: 'availableBalance',
    label: '可用余额',
    search: { hidden: true },
    table: {
      width: 120,
      align: 'right',
      slots: {
        default: (data: any) => <span class="amount-em">{fmtAmount(data.availableBalance)}</span>
      }
    }
  },
  {
    field: 'totalIncome',
    label: '累计收入',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span class="amount-in">{fmtAmount(data.totalIncome)}</span>
      }
    }
  },
  {
    field: 'totalExpense',
    label: '累计支出',
    search: { hidden: true },
    table: {
      width: 110,
      align: 'right',
      slots: {
        default: (data: any) => <span class="amount-out">{fmtAmount(data.totalExpense)}</span>
      }
    }
  },
  {
    field: 'totalFee',
    label: '累计手续费',
    search: { hidden: true },
    table: {
      width: 120,
      align: 'right',
      slots: {
        default: (data: any) => <span>{fmtAmount(data.totalFee)}</span>
      }
    }
  },
  {
    field: 'status',
    label: '账户状态',
    search: { hidden: true },
    table: {
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag type={data.status === 1 ? 'success' : 'warning'} size="small">
            {data.statusName}
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
      width: 200,
      align: 'center',
      slots: {
        default: (data: any) => {
          const row = data.row as MerchantAccountItem
          return (
            <>
              <BaseButton size="small" type="primary" link onClick={() => openFlow(row)}>
                流水
              </BaseButton>
              <BaseButton size="small" type="warning" link onClick={() => openAdjust(row)}>
                调账
              </BaseButton>
              <BaseButton
                size="small"
                link
                onClick={() => push(`/merchant/detail/${row.merchantNo}`)}
              >
                商户详情
              </BaseButton>
            </>
          )
        }
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)

// ───────── 查看流水：弹窗按账户号查询（复用 flow 接口）─────────
const flowVisible = ref(false)
const flowAccountNo = ref('')
const flows = reactive({
  list: [] as FlowItem[],
  total: 0,
  page: 1,
  pageSize: 5,
  loading: false
})
const openFlow = async (row: MerchantAccountItem) => {
  flowAccountNo.value = row.accountNo
  flows.page = 1
  flowVisible.value = true
  loadFlows()
}
const loadFlows = async () => {
  flows.loading = true
  try {
    const res = await getFlowListByAccountApi(flowAccountNo.value, {
      page: flows.page,
      pageSize: flows.pageSize
    })
    const page = res.data as PageResult<FlowItem>
    flows.list = page?.records || []
    flows.total = page?.total || 0
  } finally {
    flows.loading = false
  }
}
const changeFlowPage = (p: number) => {
  flows.page = p
  loadFlows()
}

// ───────── 超管调账：accountType=2 走商户资金账户 ─────────
const adjustVisible = ref(false)
const adjustTarget = ref<MerchantAccountItem>()
const adjustFormRef = ref<FormInstance>()
const adjusting = ref(false)
const adjustForm = reactive({
  accountNo: '',
  direction: 1 as 1 | 2, // 1-加款 2-扣款
  amount: undefined as number | undefined,
  remark: ''
})
const adjustRules = {
  amount: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        // el-input-number 清空时会回传 null，统一按无效处理
        if (value === null || value === undefined || value <= 0) {
          callback(new Error('调账金额必须大于 0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  remark: [
    {
      required: true,
      message: '调账原因不能为空',
      trigger: 'blur'
    }
  ]
}
const openAdjust = (row: MerchantAccountItem) => {
  adjustTarget.value = row
  adjustForm.accountNo = row.accountNo
  adjustForm.direction = 1
  adjustForm.amount = undefined
  adjustForm.remark = ''
  adjustVisible.value = true
}
const submitAdjust = async () => {
  const valid = await adjustFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const { direction, amount, remark } = adjustForm
  if (amount === undefined || amount <= 0) return
  adjusting.value = true
  try {
    // 方向转换为后端约定的金额符号：加款为正、扣款为负
    await adjustAccountApi({
      accountType: 2,
      accountNo: adjustForm.accountNo,
      amount: direction === 1 ? amount : -amount,
      remark: remark.trim()
    })
    ElMessage.success('调账成功')
    adjustVisible.value = false
    getList()
  } finally {
    adjusting.value = false
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

    <!-- 查看流水弹窗 -->
    <Dialog v-model="flowVisible" title="资金流水" width="900px">
      <div class="flow-account mt-0">
        账户号：<span class="num">{{ flowAccountNo }}</span>
      </div>
      <el-table :data="flows.list" v-loading="flows.loading" max-height="480">
        <el-table-column prop="flowNo" label="流水号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="flowTypeName" label="流水类型" width="100" />
        <el-table-column label="变动金额" width="120" align="right">
          <template #default="{ row } = {}">
            <span :class="amountColorClass(row?.amount)">{{ fmtSignedAmount(row?.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="余额变化" width="160" align="right">
          <template #default="{ row } = {}">
            {{ fmtAmount(row?.beforeBalance) }} → {{ fmtAmount(row?.afterBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
      <el-pagination
        class="mt-10px justify-end"
        small
        layout="total, prev, pager, next"
        :total="flows.total"
        :page-size="flows.pageSize"
        :current-page="flows.page"
        @current-change="changeFlowPage"
      />
    </Dialog>

    <!-- 超管调账弹窗 -->
    <Dialog v-model="adjustVisible" title="超管调账（商户账户）" width="520px">
      <div v-if="adjustTarget" class="flow-account">
        当前账户：<span class="num">{{ adjustTarget.accountNo }}</span>
        <span class="adjust-balace">
          · 余额 {{ fmtAmount(adjustTarget.balance) }} · 冻结
          {{ fmtAmount(adjustTarget.frozenAmount) }} · 可用
          {{ fmtAmount(adjustTarget.availableBalance) }}
        </span>
      </div>
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="90px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="adjustForm.direction">
            <el-radio :value="1">加款</el-radio>
            <el-radio :value="2">扣款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调账金额" prop="amount">
          <el-input-number
            v-model="adjustForm.amount"
            :min="0.01"
            :precision="2"
            :step="100"
            :controls="false"
            placeholder="请输入金额（元）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调账原因" prop="remark">
          <el-input
            v-model="adjustForm.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="调账原因（必填，审计留痕）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <ElButton type="primary" :loading="adjusting" @click="submitAdjust">确认调账</ElButton>
        <ElButton @click="adjustVisible = false">取消</ElButton>
      </template>
    </Dialog>
  </ContentWrap>
</template>

<style lang="less" scoped>
.flow-account {
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--el-text-color-primary);
  word-break: break-all;

  .num {
    font-weight: 600;
  }
}

.adjust-balace {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.amount-em {
  font-weight: 700;
}

.amount-in {
  color: var(--el-color-success);
}

.amount-out {
  color: var(--el-color-danger);
}
</style>
