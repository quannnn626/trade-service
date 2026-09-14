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

// 后端只返回有记录的类型，这里按 9 种类型补零，保证每天的表格结构一致
const itemMap = computed(() => {
  const map = new Map<number, DailySummaryItem>()
  summary.value?.items?.forEach((item) => map.set(item.flowType, item))
  return map
})

const rows = computed(() =>
  flowTypeOptions.map((option) => {
    const item = itemMap.value.get(option.value)
    return {
      flowType: option.value,
      flowTypeName: item?.flowTypeName || option.label,
      count: item?.count || 0,
      amount: Number(item?.amount || 0)
    }
  })
)

const amountOf = (flowType: number) => Math.abs(Number(itemMap.value.get(flowType)?.amount || 0))

const totalCount = computed(() => summary.value?.totalCount || 0)
// 口径说明：接口按流水表汇总，收入=商户收款到账（已扣手续费）、支出=用户付款
// 手续费取流水类型 3，但支付流程目前不写该类型流水，所以恒为 0（见待修复问题清单）
const incomeAmount = computed(() => amountOf(2))
const feeAmount = computed(() => amountOf(3))
const refundAmount = computed(() => amountOf(4))
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
      <ElCol :span="6">
        <el-card shadow="never">
          <el-statistic title="流水总笔数" :value="totalCount">
            <template #prefix>
              <Icon icon="vi-ep:document" />
            </template>
            <template #suffix>笔</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :span="6">
        <el-card shadow="never">
          <el-statistic title="收款金额（商户到账）" :value="incomeAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:money" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :span="6">
        <el-card shadow="never">
          <el-statistic title="手续费" :value="feeAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:coin" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
      <ElCol :span="6">
        <el-card shadow="never">
          <el-statistic title="退款金额" :value="refundAmount" :precision="2">
            <template #prefix>
              <Icon icon="vi-ep:refresh-left" />
            </template>
            <template #suffix>元</template>
          </el-statistic>
        </el-card>
      </ElCol>
    </ElRow>

    <el-card shadow="never" class="detail-card">
      <div class="detail-title">
        按流水类型汇总
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
        金额按流水方向带符号（支出为负、收入为正）；「退款支出」是商户侧扣款、「退款收入」是用户侧到账，
        同一笔退款会各记一条，统计退款金额时只取「退款支出」一侧。
        <br />
        手续费按流水类型「手续费」统计，支付流程目前未写入该类型流水，故该行固定为 0.00 （订单表已有
        fee_amount 字段，待后端按日汇总后补齐）。
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
