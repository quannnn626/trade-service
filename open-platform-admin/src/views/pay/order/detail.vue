<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { BaseButton } from '@/components/Button'
import { getPayOrderDetailApi } from '@/api/pay/order'
import type { PayOrderDetail } from '@/api/pay/order/types'
import { getFlowListByPaymentApi } from '@/api/flow'
import type { FlowItem } from '@/api/flow/types'
import { getNotifyPageApi } from '@/api/notify'
import type { NotifyItem } from '@/api/notify/types'
import { getRefundPageApi } from '@/api/refund'
import type { RefundItem } from '@/api/refund/types'
import type { PageResult } from '@/api/pay/order/types'
import {
  fmtAmount,
  fmtTime,
  notifyStatusTagType,
  refundStatusTagType,
  statusTagType
} from '../common'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineOptions({
  name: 'PayOrderDetail'
})

const route = useRoute()
const { push } = useRouter()
const paymentNo = route.params.paymentNo as string

// ───────── 订单信息 ─────────
const detail = ref<PayOrderDetail>()
const loading = ref(false)
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getPayOrderDetailApi(paymentNo)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

// ───────── 三个关联记录 Tab（流水/回调/退款）─────────
interface TabState<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  loading: boolean
}

function createTabState<T>(
  fetcher: (page: number, pageSize: number) => Promise<IResponse<PageResult<T>>>
) {
  const state = reactive({
    list: [] as T[],
    total: 0,
    page: 1,
    pageSize: 5,
    loading: false
  }) as TabState<T>
  const load = async () => {
    state.loading = true
    try {
      const res = await fetcher(state.page, state.pageSize)
      state.list = res.data?.records || []
      state.total = res.data?.total || 0
    } finally {
      state.loading = false
    }
  }
  const changePage = (p: number) => {
    state.page = p
    load()
  }
  return { state, load, changePage }
}

const flows = createTabState<FlowItem>((page, pageSize) =>
  getFlowListByPaymentApi(paymentNo, { page, pageSize })
)
const notifies = createTabState<NotifyItem>((page, pageSize) =>
  getNotifyPageApi({ page, pageSize, paymentNo })
)
const refunds = createTabState<RefundItem>((page, pageSize) =>
  getRefundPageApi({ page, pageSize, paymentNo })
)

loadDetail()
flows.load()
notifies.load()
refunds.load()

// 变动金额：负=支出（红） 正=收入（绿）
const amountColor = (v: number) => (v < 0 ? 'amount-out' : 'amount-in')
const fmtFlowAmount = (v: number) => (v > 0 ? '+' : '') + fmtAmount(v)
</script>

<template>
  <ContentWrap>
    <div class="detail-header">
      <BaseButton type="primary" size="small" @click="push('/pay/order')">返回列表</BaseButton>
      <span class="detail-title">支付订单详情</span>
      <span class="detail-no">{{ paymentNo }}</span>
    </div>

    <!-- 结果条：状态（支付宝式成功态配色）+ 金额大字 + 时间 -->
    <div v-loading="loading" class="result-bar mt-10px">
      <div class="result-state" :class="`result-${statusTagType(detail?.status)}`">
        <span class="state-dot"></span>
        <span class="result-status">{{ detail?.statusName || '-' }}</span>
      </div>
      <div class="result-money">
        <span class="money">{{ fmtAmount(detail?.amount) }}</span>
      </div>
      <div class="result-meta">
        <div class="meta-row">
          <span class="meta-label">支付时间</span>
          <span>{{ fmtTime(detail?.payTime) }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-label">支付渠道</span>
          <span>{{ detail?.channelName || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 行式信息网格：空字段自动隐藏，字段整齐一行一值 -->
    <div class="group mt-10px">
      <div class="group-title">订单与商品</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.paymentNo">
          <div class="field-label">支付单号</div>
          <div class="field-value num">{{ detail.paymentNo }}</div>
        </div>
        <div class="field" v-if="detail?.orderNo">
          <div class="field-label">商户订单号</div>
          <div class="field-value num">{{ detail.orderNo }}</div>
        </div>
        <div class="field" v-if="detail?.merchantPaymentNo">
          <div class="field-label">商城支付单号</div>
          <div class="field-value num">{{ detail.merchantPaymentNo }}</div>
        </div>
        <div class="field" v-if="detail?.subject">
          <div class="field-label">商品标题</div>
          <div class="field-value">{{ detail.subject }}</div>
        </div>
        <div class="field" v-if="detail?.description">
          <div class="field-label">订单描述</div>
          <div class="field-value">{{ detail.description }}</div>
        </div>
        <div class="field" v-if="detail?.channelName">
          <div class="field-label">支付渠道</div>
          <div class="field-value">{{ detail.channelName }}</div>
        </div>
        <div class="field" v-if="detail?.channelCode">
          <div class="field-label">渠道编码</div>
          <div class="field-value">{{ detail.channelCode }}</div>
        </div>
        <div class="field" v-if="detail?.clientIp">
          <div class="field-label">客户端 IP</div>
          <div class="field-value">{{ detail.clientIp }}</div>
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
        <div class="field" v-if="detail?.userNo">
          <div class="field-label">用户编号</div>
          <div class="field-value num">{{ detail.userNo }}</div>
        </div>
        <div class="field" v-if="detail?.userName">
          <div class="field-label">用户名</div>
          <div class="field-value">{{ detail.userName }}</div>
        </div>
        <div class="field" v-if="detail?.userPhone">
          <div class="field-label">手机号</div>
          <div class="field-value">{{ detail.userPhone }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.notifyUrl">
          <div class="field-label">商户回调地址</div>
          <div class="field-value">{{ detail.notifyUrl }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.attach">
          <div class="field-label">附加数据</div>
          <div class="field-value">{{ detail.attach }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">金额与结算</div>
      <div class="info-grid">
        <div class="field">
          <div class="field-label">支付金额</div>
          <div class="field-value amount-em">{{ fmtAmount(detail?.amount) }}</div>
        </div>
        <div class="field" v-if="detail?.feeAmount !== undefined">
          <div class="field-label">手续费</div>
          <div class="field-value">{{ fmtAmount(detail?.feeAmount) }}</div>
        </div>
        <div class="field" v-if="detail?.settleAmount !== undefined">
          <div class="field-label">结算金额</div>
          <div class="field-value amount-em">{{ fmtAmount(detail?.settleAmount) }}</div>
        </div>
        <div class="field" v-if="detail?.settleStatus !== undefined">
          <div class="field-label">结算状态</div>
          <div class="field-value" :class="detail.settleStatus === 1 ? 'settled' : ''">
            {{ detail.settleStatus === 1 ? '已结算' : detail.settleStatus === 0 ? '未结算' : '-' }}
          </div>
        </div>
        <div class="field" v-if="detail?.settleTime">
          <div class="field-label">结算时间</div>
          <div class="field-value">{{ fmtTime(detail.settleTime) }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.returnUrl">
          <div class="field-label">跳转地址</div>
          <div class="field-value">{{ detail.returnUrl }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">时间与关单</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.createTime">
          <div class="field-label">创建时间</div>
          <div class="field-value">{{ fmtTime(detail.createTime) }}</div>
        </div>
        <div class="field" v-if="detail?.payTime">
          <div class="field-label">支付时间</div>
          <div class="field-value">{{ fmtTime(detail.payTime) }}</div>
        </div>
        <div class="field" v-if="detail?.updateTime">
          <div class="field-label">更新时间</div>
          <div class="field-value">{{ fmtTime(detail.updateTime) }}</div>
        </div>
        <div class="field" v-if="detail?.expireTime">
          <div class="field-label">过期时间</div>
          <div class="field-value">{{ fmtTime(detail.expireTime) }}</div>
        </div>
        <div class="field" v-if="detail?.timeoutExpire">
          <div class="field-label">自动关单时间</div>
          <div class="field-value">{{ fmtTime(detail.timeoutExpire) }}</div>
        </div>
        <div class="field" v-if="detail?.closeTime">
          <div class="field-label">关单时间</div>
          <div class="field-value">{{ fmtTime(detail.closeTime) }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.closeReason">
          <div class="field-label">关单原因</div>
          <div class="field-value">{{ detail.closeReason }}</div>
        </div>
      </div>
    </div>

    <el-tabs class="mt-10px" type="border-card">
      <!-- 资金流水 -->
      <el-tab-pane label="资金流水" lazy>
        <el-table :data="flows.state.list" v-loading="flows.state.loading">
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
          :total="flows.state.total"
          :page-size="flows.state.pageSize"
          :current-page="flows.state.page"
          @current-change="flows.changePage"
        />
      </el-tab-pane>

      <!-- 回调通知 -->
      <el-tab-pane label="回调通知" lazy>
        <el-table :data="notifies.state.list" v-loading="notifies.state.loading">
          <el-table-column prop="notifyTypeName" label="通知类型" width="110" />
          <el-table-column label="状态" width="110">
            <template #default="{ row } = {}">
              <el-tag :type="notifyStatusTagType(row?.notifyStatus)">{{
                row?.notifyStatusName
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="重试" width="90">
            <template #default="{ row } = {}">{{ row?.retryCount }}/{{ row?.maxRetry }}</template>
          </el-table-column>
          <el-table-column prop="nextRetryTime" label="下次重试" width="170">
            <template #default="{ row } = {}">{{ row?.nextRetryTime || '-' }}</template>
          </el-table-column>
          <el-table-column prop="lastError" label="最后错误" min-width="180" show-overflow-tooltip>
            <template #default="{ row } = {}">{{ row?.lastError || '-' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </el-table>
        <el-pagination
          class="mt-10px justify-end"
          small
          layout="total, prev, pager, next"
          :total="notifies.state.total"
          :page-size="notifies.state.pageSize"
          :current-page="notifies.state.page"
          @current-change="notifies.changePage"
        />
      </el-tab-pane>

      <!-- 退款记录 -->
      <el-tab-pane label="退款记录" lazy>
        <el-table :data="refunds.state.list" v-loading="refunds.state.loading">
          <el-table-column prop="refundNo" label="退款单号" min-width="180" show-overflow-tooltip />
          <el-table-column
            prop="merchantRefundNo"
            label="商户退款单号"
            min-width="160"
            show-overflow-tooltip
          />
          <el-table-column prop="refundTypeName" label="类型" width="90" />
          <el-table-column label="申请金额" width="110" align="right">
            <template #default="{ row } = {}">{{ fmtAmount(row?.applyAmount) }}</template>
          </el-table-column>
          <el-table-column label="实退金额" width="110" align="right">
            <template #default="{ row } = {}">{{ fmtAmount(row?.actualAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row } = {}">
              <el-tag :type="refundStatusTagType(row?.status)">{{ row?.statusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditStatusName" label="审核" width="100" />
          <el-table-column
            prop="refundReason"
            label="退款原因"
            min-width="140"
            show-overflow-tooltip
          >
            <template #default="{ row } = {}">{{ row?.refundReason || '-' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </el-table>
        <el-pagination
          class="mt-10px justify-end"
          small
          layout="total, prev, pager, next"
          :total="refunds.state.total"
          :page-size="refunds.state.pageSize"
          :current-page="refunds.state.page"
          @current-change="refunds.changePage"
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

/* ───────── 结果条：状态 + 金额大字 + 时间（支付宝成功页式主次结构）───────── */
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

/* 状态色与状态枚举 tag 色一致 */
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

  .money-label {
    display: block;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 6px;
  }

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

/* 已结算状态绿色 */
.settled {
  font-weight: 600;
  color: var(--el-color-success);
}

/* 表格资金流水：负=支出（红）正=收入（绿） */
.amount-in {
  color: var(--el-color-success);
}

.amount-out {
  color: var(--el-color-danger);
}
</style>
