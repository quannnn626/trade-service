<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { BaseButton } from '@/components/Button'
import { getRefundDetailApi } from '@/api/refund'
import type { RefundDetail } from '@/api/refund/types'
import { getFlowListByPaymentApi } from '@/api/flow'
import type { FlowItem } from '@/api/flow/types'
import type { PageResult } from '@/api/pay/order/types'
import { fmtAmount, fmtTime, refundStatusTagType } from '../common'
import { ElPagination, ElTabPane, ElTable, ElTableColumn, ElTabs, ElTag } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineOptions({
  name: 'PayRefundDetail'
})

const route = useRoute()
const { push } = useRouter()
const refundNo = route.params.refundNo as string

// ───────── 退款单信息 ─────────
const detail = ref<RefundDetail>()
const loading = ref(false)
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getRefundDetailApi(refundNo)
    detail.value = res.data
  } finally {
    loading.value = false
  }
  // 流水按支付单号查，必须等详情返回拿到 paymentNo 后再发
  loadFlows()
}

// ───────── 关联资金流水（按支付单号复用 flow 接口）─────────
const flows = reactive({
  list: [] as FlowItem[],
  total: 0,
  page: 1,
  pageSize: 5,
  loading: false
})
const loadFlows = async () => {
  const paymentNo = detail.value?.paymentNo
  if (!paymentNo) return
  flows.loading = true
  try {
    const res = await getFlowListByPaymentApi(paymentNo, {
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
const changePage = (p: number) => {
  flows.page = p
  loadFlows()
}

loadDetail()

// 变动金额：负=支出（红） 正=收入（绿）
const amountColor = (v: number) => (v < 0 ? 'amount-out' : 'amount-in')
const fmtFlowAmount = (v: number) => (v > 0 ? '+' : '') + fmtAmount(v)
</script>

<template>
  <ContentWrap>
    <div class="detail-header">
      <BaseButton type="primary" size="small" @click="push('/pay/refund')">返回列表</BaseButton>
      <span class="detail-title">退款订单详情</span>
      <span class="detail-no">{{ refundNo }}</span>
    </div>

    <!-- 结果条：状态（退款状态配色）+ 退款金额大字 + 时间 -->
    <div v-loading="loading" class="result-bar mt-10px">
      <div class="result-state" :class="`result-${refundStatusTagType(detail?.status)}`">
        <span class="state-dot"></span>
        <span class="result-status">{{ detail?.statusName || '-' }}</span>
      </div>
      <div class="result-money">
        <span class="money">{{ fmtAmount(detail?.refundAmount) }}</span>
      </div>
      <div class="result-meta">
        <div class="meta-row">
          <span class="meta-label">申请时间</span>
          <span>{{ fmtTime(detail?.applyTime) }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-label">完成时间</span>
          <span>{{ fmtTime(detail?.finishTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 行式信息网格：空字段自动隐藏，字段整齐一行一值 -->
    <div class="group mt-10px">
      <div class="group-title">退款单信息</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.refundNo">
          <div class="field-label">退款单号</div>
          <div class="field-value num">{{ detail.refundNo }}</div>
        </div>
        <div class="field" v-if="detail?.paymentNo">
          <div class="field-label">支付单号</div>
          <div class="field-value num">{{ detail.paymentNo }}</div>
        </div>
        <div class="field" v-if="detail?.merchantRefundNo">
          <div class="field-label">商户退款单号</div>
          <div class="field-value num">{{ detail.merchantRefundNo }}</div>
        </div>
        <div class="field" v-if="detail?.refundTypeName">
          <div class="field-label">退款类型</div>
          <div class="field-value">{{ detail.refundTypeName }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.refundReason">
          <div class="field-label">退款原因</div>
          <div class="field-value">{{ detail.refundReason }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">商户与用户</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.merchantNo">
          <div class="field-label">商户号</div>
          <div class="field-value num">{{ detail.merchantNo }}</div>
        </div>
        <div class="field" v-if="detail?.merchantName">
          <div class="field-label">商户名称</div>
          <div class="field-value">{{ detail.merchantName }}</div>
        </div>
        <div class="field" v-if="detail?.userId !== undefined">
          <div class="field-label">用户 ID</div>
          <div class="field-value num">{{ detail.userId }}</div>
        </div>
        <div class="field" v-if="detail?.userName">
          <div class="field-label">用户名称</div>
          <div class="field-value">{{ detail.userName }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.notifyUrl">
          <div class="field-label">退款回调地址</div>
          <div class="field-value">{{ detail.notifyUrl }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">金额与渠道</div>
      <div class="info-grid">
        <div class="field">
          <div class="field-label">退款金额</div>
          <div class="field-value amount-em">{{ fmtAmount(detail?.refundAmount) }}</div>
        </div>
        <div class="field" v-if="detail?.applyAmount !== undefined">
          <div class="field-label">申请金额</div>
          <div class="field-value">{{ fmtAmount(detail.applyAmount) }}</div>
        </div>
        <div class="field" v-if="detail?.actualAmount !== undefined">
          <div class="field-label">实际退款</div>
          <div class="field-value amount-em">{{ fmtAmount(detail.actualAmount) }}</div>
        </div>
        <div class="field" v-if="detail?.feeRefund !== undefined">
          <div class="field-label">退还手续费</div>
          <div class="field-value">
            {{ detail.feeRefund ? fmtAmount(detail.feeRefund) : '-' }}
          </div>
        </div>
        <div class="field" v-if="detail?.refundChannel !== undefined">
          <div class="field-label">退款渠道</div>
          <div class="field-value">{{ detail.refundChannel === 1 ? '原路退回' : '-' }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">状态与审核</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.statusName">
          <div class="field-label">退款状态</div>
          <div class="field-value">
            <el-tag :type="refundStatusTagType(detail.status)" size="small">{{
              detail.statusName
            }}</el-tag>
          </div>
        </div>
        <div class="field" v-if="detail?.auditStatusName">
          <div class="field-label">审核状态</div>
          <div class="field-value">{{ detail.auditStatusName }}</div>
        </div>
        <div class="field" v-if="detail?.auditorName">
          <div class="field-label">审核人</div>
          <div class="field-value">{{ detail.auditorName }}</div>
        </div>
        <div class="field" v-if="detail?.auditTime">
          <div class="field-label">审核时间</div>
          <div class="field-value">{{ fmtTime(detail.auditTime) }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.failReason">
          <div class="field-label">失败原因</div>
          <div class="field-value">{{ detail.failReason }}</div>
        </div>
      </div>
    </div>

    <!-- 关联支付订单（VO 内嵌回填） -->
    <div class="group mt-10px">
      <div class="group-title">关联支付订单</div>
      <div class="info-grid" v-if="detail?.order">
        <div class="field" v-if="detail.order.orderNo">
          <div class="field-label">商户订单号</div>
          <div class="field-value num">{{ detail.order.orderNo }}</div>
        </div>
        <div class="field" v-if="detail.order.subject">
          <div class="field-label">商品标题</div>
          <div class="field-value">{{ detail.order.subject }}</div>
        </div>
        <div class="field" v-if="detail.order.amount !== undefined">
          <div class="field-label">订单金额</div>
          <div class="field-value amount-em">{{ fmtAmount(detail.order.amount) }}</div>
        </div>
        <div class="field" v-if="detail.order.statusName">
          <div class="field-label">订单状态</div>
          <div class="field-value">{{ detail.order.statusName }}</div>
        </div>
      </div>
      <div class="info-grid" v-else>
        <div class="field">
          <div class="field-label">关联支付订单</div>
          <div class="field-value">-</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">时间记录</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.createTime">
          <div class="field-label">创建时间</div>
          <div class="field-value">{{ fmtTime(detail.createTime) }}</div>
        </div>
        <div class="field" v-if="detail?.updateTime">
          <div class="field-label">更新时间</div>
          <div class="field-value">{{ fmtTime(detail.updateTime) }}</div>
        </div>
      </div>
    </div>

    <el-tabs class="mt-10px" type="border-card">
      <!-- 资金流水 -->
      <el-tab-pane label="资金流水" lazy>
        <el-table :data="flows.list" v-loading="flows.loading">
          <el-table-column prop="flowNo" label="流水号" min-width="180" show-overflow-tooltip />
          <el-table-column prop="accountTypeName" label="账户类型" width="90" />
          <el-table-column prop="accountNo" label="账户编号" width="170" />
          <el-table-column prop="flowTypeName" label="流水类型" width="100" />
          <el-table-column label="变动金额" width="120" align="right">
            <!-- 解构加参数默认值：EP 在 tab 隐藏容器初始化列时会无参调用插槽，直接解构会崩 -->
            <template #default="{ row } = {}">
              <span :class="amountColor(row?.amount)">{{ fmtFlowAmount(row?.amount) }}</span>
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
          @current-change="changePage"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<style lang="less" scoped>
.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .detail-title {
    font-size: 16px;
    font-weight: 600;
  }

  .detail-no {
    font-weight: 600;
    color: var(--el-text-color-secondary);
  }
}

/* ───────── 结果条：状态 + 金额大字 + 时间 ───────── */
.result-bar {
  display: flex;
  align-items: center;
  padding: 24px 28px;
  background: #fff;
  border-radius: 8px;
}

.result-state {
  display: flex;
  align-items: center;
  gap: 10px;

  .state-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: currentColor;
  }

  .result-status {
    font-size: 20px;
    font-weight: 600;
  }
}

/* 状态色与退款状态 tag 色一致 */
.result-success {
  color: var(--el-color-success);
}

.result-danger {
  color: var(--el-color-danger);
}

.result-warning {
  color: var(--el-color-warning);
}

.result-info {
  color: var(--el-color-info);
}

.result-primary {
  color: var(--el-color-primary);
}

.result-money {
  flex: 1;
  margin-left: 48px;

  .money {
    font-size: 32px;
    font-weight: 700;
    line-height: 1;
    color: var(--el-text-color-primary);
  }
}

.result-meta {
  margin-left: 48px;
  text-align: right;

  .meta-row {
    font-size: 14px;
    color: var(--el-text-color-primary);

    & + .meta-row {
      margin-top: 8px;
    }
  }

  .meta-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-right: 10px;
  }
}

/* ───────── 信息分组：组标题 + 两列字段网格 ───────── */
.group-title {
  position: relative;
  padding-left: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 14px;
    border-radius: 2px;
    background: var(--el-color-primary);
  }
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 48px;
  margin-top: 6px;
}

.field {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 9px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);

  .field-label {
    flex-shrink: 0;
    width: 96px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: right;
  }

  .field-value {
    font-size: 14px;
    color: var(--el-text-color-primary);
    word-break: break-all;
  }
}

/* 长文本/超链接占满整行 */
.field-wide {
  grid-column: span 2;
}

/* 单号类字段加粗 */
.num {
  font-weight: 600;
}

/* 关键金额强调 */
.amount-em {
  font-weight: 700;
}

/* 表格资金流水：负=支出（红）正=收入（绿） */
.amount-in {
  color: var(--el-color-success);
}

.amount-out {
  color: var(--el-color-danger);
}
</style>
