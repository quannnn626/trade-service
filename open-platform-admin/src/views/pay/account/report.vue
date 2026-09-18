<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import { getDailySummaryApi } from '@/api/flow'
import type { DailySummaryItem, DailySummaryResult } from '@/api/flow/types'
import { amountColorClass, flowTypeOptions, fmtSignedAmount } from '../common'
import {
  ElButton,
  ElCard,
  ElCol,
  ElDatePicker,
  ElRow,
  ElStatistic,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { computed, ref } from 'vue'

defineOptions({
  name: 'DailySummaryReport'
})

/** 本地日期字符串（yyyy-MM-dd），不能用 toISOString，会差 8 小时 */
const todayStr = () => {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const queryDate = ref(todayStr())
const loading = ref(false)
const summary = ref<DailySummaryResult>()

const loadSummary = async () => {
  loading.value = true
  try {
    const res = await getDailySummaryApi(queryDate.value)
    summary.value = res.data
  } finally {
    loading.value = false
  }
}
loadSummary()

const backToToday = () => {
  queryDate.value = todayStr()
  loadSummary()
}

/** 不允许选未来日期：当天之后的流水不存在 */
const disabledFuture = (date: Date) => date.getTime() > Date.now()

/** 手续费流水类型（AccountFlowTypeEnum.FEE），流水表中不存在该类型记录，表格需排除 */
const FEE_FLOW_TYPE = 3

// 后端只返回有记录的类型，这里按流水类型补零，保证每天的表格结构一致
const itemMap = computed(() => {
  const map = new Map<number, DailySummaryItem>()
  summary.value?.items?.forEach((item) => map.set(item.flowType, item))
  return map
})

// 手续费不产生余额变动（商户按「交易额 - 手续费」净额入账），流水表永远没有这一行，
// 列出只会是恒 0.00，与上方卡片的真实手续费同屏矛盾，故本地过滤掉
const rows = computed(() =>
  flowTypeOptions
    .filter((option) => option.value !== FEE_FLOW_TYPE)
    .map((option) => {
      const item = itemMap.value.get(option.value)
      return {
        flowType: option.value,
        flowTypeName: item?.flowTypeName || option.label,
        count: item?.count || 0,
        amount: Number(item?.amount || 0)
      }
    })
)

// 卡片是交易/退款口径（订单表 + 退款单表），下方表格是流水口径，两者不是同一批数据
const payCount = computed(() => summary.value?.payCount || 0)
const tradeAmount = computed(() => Number(summary.value?.tradeAmount || 0))
const feeAmount = computed(() => Number(summary.value?.feeAmount || 0))
const settleAmount = computed(() => Number(summary.value?.settleAmount || 0))
const refundCount = computed(() => summary.value?.refundCount || 0)
const refundAmount = computed(() => Number(summary.value?.refundAmount || 0))
</script>

<template>
  <ContentWrap>
    <div class="report-filter">
      <span class="filter-label">汇总日期</span>
      <el-date-picker
        v-model="queryDate"
        type="date"
        value-format="YYYY-MM-DD"
        :clearable="false"
        :disabled-date="disabledFuture"
        placeholder="选择日期"
        style="width: 170px"
        @change="loadSummary"
      />
      <ElButton type="primary" :loading="loading" @click="loadSummary">查询</ElButton>
      <ElButton @click="backToToday">今天</ElButton>
    </div>

    <ElRow :gutter="16" class="kpi-row" v-loading="loading">
      <ElCol :sm="12" :md="8" :lg="4">
        <el-card shadow="never">
          <el-statistic title="支付成功笔数" :value="payCount">
            <template #prefix>
              <Icon icon="vi-ep:document-checked" />
            </template>
            <template #suffix>笔</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :sm="12" :md="8" :lg="4">
        <el-card shadow="never">
          <el-statistic title="交易额" :value="tradeAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:money" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :sm="12" :md="8" :lg="4">
        <el-card shadow="never">
          <el-statistic title="手续费" :value="feeAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:coin" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :sm="12" :md="8" :lg="4">
        <el-card shadow="never">
          <el-statistic title="商户到账" :value="settleAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:wallet" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :sm="12" :md="8" :lg="4">
        <el-card shadow="never">
          <el-statistic title="退款成功笔数" :value="refundCount">
            <template #prefix>
              <Icon icon="vi-ep:refresh-left" />
            </template>
            <template #suffix>笔</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :sm="12" :md="8" :lg="4">
        <el-card shadow="never">
          <el-statistic title="退款金额" :value="refundAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:credit-card" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
    </ElRow>

    <el-card shadow="never" class="detail-card">
      <div class="detail-title">
        按流水类型汇总（资金流水口径）
        <span class="detail-date">{{ summary?.date }}</span>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="flowTypeName" label="流水类型" min-width="120" />
        <el-table-column label="笔数" width="120" align="right">
          <template #default="{ row }">
            <span class="num-col">{{ row.count }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额合计（元）" min-width="160" align="right">
          <template #default="{ row }">
            <span class="num-col" :class="row.count ? amountColorClass(row.amount) : ''">
              {{ row.count ? fmtSignedAmount(row.amount) : '0.00' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div class="detail-tip">
        上方卡片为交易/退款口径：交易额、手续费、商户到账取自订单表（交易额 - 手续费 = 商户到账），
        退款取自退款单表的成功退款。本表为资金流水口径，是账户余额的实际变动明细。
        <br />
        金额按流水方向带符号（支出为负、收入为正）；「退款支出」是商户侧扣款、「退款收入」是用户侧到账，
        同一笔退款会各记一条。
        <br />
        手续费不产生余额变动（商户按「交易额 - 手续费」净额入账），流水表没有该类型记录，故本表不列；
        手续费请以上方卡片为准。
      </div>
    </el-card>
  </ContentWrap>
</template>

<style lang="less" scoped>
.report-filter {
  display: flex;
  align-items: center;
  margin-bottom: 16px;

  .filter-label {
    margin-right: 8px;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }

  .el-button + .el-button {
    margin-left: 8px;
  }
}

.kpi-row {
  margin-bottom: 16px;

  :deep(.el-statistic__head) {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  :deep(.el-statistic__number) {
    font-size: 22px;
    font-weight: 600;
  }
}

.detail-card {
  .detail-title {
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);

    .detail-date {
      margin-left: 8px;
      font-size: 13px;
      font-weight: 400;
      color: var(--el-text-color-secondary);
    }
  }

  .detail-tip {
    margin-top: 12px;
    font-size: 12px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }
}

// 金额列纵向对齐更易比对
.num-col {
  font-variant-numeric: tabular-nums;
}

.amount-in {
  color: var(--el-color-success);
}

.amount-out {
  color: var(--el-color-danger);
}
</style>
