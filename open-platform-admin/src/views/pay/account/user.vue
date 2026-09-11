<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { Dialog } from '@/components/Dialog'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import {
  adjustAccountApi,
  disableAccountApi,
  enableAccountApi,
  getAccountPageApi
} from '@/api/account'
import { getFlowListByAccountApi } from '@/api/flow'
import type { AccountItem } from '@/api/account/types'
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
  ElMessageBox,
  ElPagination,
  ElRadio,
  ElRadioGroup,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus'
import type { FormInstance } from 'element-plus'
import { reactive, ref, unref } from 'vue'

defineOptions({
  name: 'UserAccountList'
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
    if (s.accountNo) params.accountNo = s.accountNo
    if (s.username) params.username = s.username
    if (s.phone) params.phone = s.phone
    const res = await getAccountPageApi(params as any)
    const page = res.data as PageResult<AccountItem>
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
    field: 'accountNo',
    label: '账户号',
    minWidth: 180,
    search: {
      component: 'Input',
      componentProps: { placeholder: '账户号（UA 开头）', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'username',
    label: '用户名',
    minWidth: 120,
    search: {
      component: 'Input',
      componentProps: { placeholder: '用户名', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'userNo',
    label: '用户编号',
    minWidth: 150,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'phone',
    label: '手机号',
    width: 130,
    search: {
      component: 'Input',
      componentProps: { placeholder: '手机号', clearable: true }
    }
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
        default: (data: any) => <span>{fmtAmount(data.totalIncome)}</span>
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
        default: (data: any) => <span>{fmtAmount(data.totalExpense)}</span>
      }
    }
  },
  {
    field: 'realNameAuth',
    label: '实名认证',
    search: { hidden: true },
    table: {
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) =>
          data.realNameAuth ? (
            <ElTag type="success" size="small">
              已实名
            </ElTag>
          ) : (
            <ElTag type="info" size="small">
              未实名
            </ElTag>
          )
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
          const row = data.row as AccountItem
          return (
            <>
              <BaseButton size="small" type="primary" link onClick={() => openFlow(row)}>
                流水
              </BaseButton>
              <BaseButton size="small" type="warning" link onClick={() => openAdjust(row)}>
                调账
              </BaseButton>
              {row.status === 1 ? (
                <BaseButton size="small" type="danger" link onClick={() => handleToggle(row)}>
                  禁用
                </BaseButton>
              ) : (
                <BaseButton size="small" type="success" link onClick={() => handleToggle(row)}>
                  启用
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

// ───────── 账户启停：正常→禁用（冻结），冻结→启用（恢复）─────────
const handleToggle = async (row: AccountItem) => {
  const disable = row.status === 1
  try {
    await ElMessageBox.confirm(
      disable
        ? `确认禁用账户 ${row.accountNo}？禁用后该用户将无法支付和充值。`
        : `确认启用账户 ${row.accountNo}？启用后恢复正常的支付和充值能力。`,
      disable ? '禁用账户' : '启用账户',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
    if (disable) {
      await disableAccountApi(row.accountNo)
      ElMessage.success('账户已禁用')
    } else {
      await enableAccountApi(row.accountNo)
      ElMessage.success('账户已启用')
    }
    getList()
  } catch {
    // 用户取消或接口报错（报错已由全局拦截器提示）
  }
}

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
const openFlow = async (row: AccountItem) => {
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

// ───────── 超管调账：正数加款、负数扣款（弹窗用方向简化输入）─────────
const adjustVisible = ref(false)
const adjustTarget = ref<AccountItem>()
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
const openAdjust = (row: AccountItem) => {
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
      accountType: 1,
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
    <Dialog v-model="adjustVisible" title="超管调账" width="520px">
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
