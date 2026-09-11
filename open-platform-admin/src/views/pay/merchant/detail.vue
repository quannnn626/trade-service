<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { BaseButton } from '@/components/Button'
import {
  disableMerchantApi,
  enableMerchantApi,
  getMerchantDetailApi,
  rotateMerchantSecretApi
} from '@/api/merchant'
import type { MerchantDetail } from '@/api/merchant/types'
import {
  fmtAmount,
  fmtRate,
  fmtTime,
  merchantAuditStatusText,
  merchantAuditTagType,
  merchantStatusTagType,
  settleTypeText
} from '../common'
import { ElButton, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineOptions({
  name: 'MerchantDetail'
})

const route = useRoute()
const { push } = useRouter()
const merchantNo = route.params.merchantNo as string

const detail = ref<MerchantDetail>()
const loading = ref(false)
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getMerchantDetailApi(merchantNo)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}
loadDetail()

// 可用余额 = 余额 - 冻结（与后端 availableBalance 口径一致）
const availableBalance = computed(() => {
  const d = detail.value
  if (!d) return undefined
  return Number(d.balance || 0) - Number(d.frozenAmount || 0)
})

// ───────── 商户启停 ─────────
const toggleLoading = ref(false)
const handleToggle = async () => {
  const d = detail.value
  if (!d) return
  const disable = d.status === 1
  try {
    await ElMessageBox.confirm(
      disable
        ? `确认禁用商户 ${d.merchantNo}？禁用后该商户无法发起支付。`
        : `确认启用商户 ${d.merchantNo}？启用后恢复正常交易能力。`,
      disable ? '禁用商户' : '启用商户',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户取消
  }
  toggleLoading.value = true
  try {
    if (disable) {
      await disableMerchantApi(d.merchantNo)
      ElMessage.success('商户已禁用')
    } else {
      await enableMerchantApi(d.merchantNo)
      ElMessage.success('商户已启用')
    }
    loadDetail()
  } finally {
    toggleLoading.value = false
  }
}

// ───────── 密钥轮换：新 appSecret 仅返回一次，弹窗展示 ─────────
const secretVisible = ref(false)
const newSecret = ref('')
const newSecretVersion = ref<number>()
const secretTip = ref('')
const rotating = ref(false)
const handleRotate = async () => {
  const d = detail.value
  if (!d) return
  try {
    await ElMessageBox.confirm(
      '轮换后旧 appSecret 立即失效，商户必须改用新密钥签名，确认轮换？',
      '轮换密钥',
      { confirmButtonText: '确认轮换', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户取消
  }
  rotating.value = true
  try {
    const res = await rotateMerchantSecretApi(d.merchantNo)
    const secret = res.data
    newSecret.value = secret?.appSecret || ''
    newSecretVersion.value = secret?.secretVersion
    secretTip.value = secret?.tip || ''
    secretVisible.value = true
    loadDetail() // 刷新密钥版本号
  } finally {
    rotating.value = false
  }
}

const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择复制')
  }
}
</script>

<template>
  <ContentWrap>
    <div class="detail-header">
      <BaseButton type="primary" size="small" @click="push('/merchant/list')">返回列表</BaseButton>
      <span class="detail-title">商户详情</span>
      <span class="detail-no">{{ merchantNo }}</span>
      <div class="detail-actions">
        <BaseButton
          v-if="detail"
          :type="detail.status === 1 ? 'danger' : 'success'"
          size="small"
          :loading="toggleLoading"
          @click="handleToggle"
        >
          {{ detail.status === 1 ? '禁用商户' : '启用商户' }}
        </BaseButton>
      </div>
    </div>

    <!-- 结果条：状态 + 商户名称 + 余额/时间 -->
    <div v-loading="loading" class="result-bar mt-10px">
      <div class="result-state" :class="`result-${merchantStatusTagType(detail?.status)}`">
        <span class="state-dot"></span>
        <span class="result-status">{{ detail?.status === 1 ? '启用' : '禁用' }}</span>
      </div>
      <div class="result-money">
        <span class="money">{{ detail?.merchantName || '-' }}</span>
      </div>
      <div class="result-meta">
        <div class="meta-row">
          <span class="meta-label">账户余额</span>
          <span>{{ fmtAmount(detail?.balance) }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-label">创建时间</span>
          <span>{{ fmtTime(detail?.createTime) }}</span>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">基本信息</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.merchantNo">
          <div class="field-label">商户编号</div>
          <div class="field-value num">{{ detail.merchantNo }}</div>
        </div>
        <div class="field" v-if="detail?.merchantName">
          <div class="field-label">商户名称</div>
          <div class="field-value">{{ detail.merchantName }}</div>
        </div>
        <div class="field" v-if="detail?.companyName">
          <div class="field-label">企业全称</div>
          <div class="field-value">{{ detail.companyName }}</div>
        </div>
        <div class="field" v-if="detail?.businessLicense">
          <div class="field-label">营业执照号</div>
          <div class="field-value num">{{ detail.businessLicense }}</div>
        </div>
        <div class="field" v-if="detail?.merchantType !== undefined">
          <div class="field-label">商户类型</div>
          <div class="field-value">{{ detail.merchantType }}</div>
        </div>
        <div class="field" v-if="detail?.status !== undefined">
          <div class="field-label">商户状态</div>
          <div class="field-value">
            <el-tag :type="merchantStatusTagType(detail.status)" size="small">
              {{ detail.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
        </div>
        <div class="field" v-if="detail?.auditStatus !== undefined">
          <div class="field-label">审核状态</div>
          <div class="field-value">
            <el-tag :type="merchantAuditTagType(detail.auditStatus)" size="small">
              {{ merchantAuditStatusText(detail.auditStatus) }}
            </el-tag>
          </div>
        </div>
        <div class="field field-wide" v-if="detail?.auditRemark">
          <div class="field-label">审核备注</div>
          <div class="field-value">{{ detail.auditRemark }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">联系人</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.contactName">
          <div class="field-label">联系人</div>
          <div class="field-value">{{ detail.contactName }}</div>
        </div>
        <div class="field" v-if="detail?.contactPhone">
          <div class="field-label">联系电话</div>
          <div class="field-value">{{ detail.contactPhone }}</div>
        </div>
        <div class="field" v-if="detail?.contactEmail">
          <div class="field-label">联系邮箱</div>
          <div class="field-value">{{ detail.contactEmail }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">结算与风控</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.settleType !== undefined">
          <div class="field-label">结算方式</div>
          <div class="field-value">{{ settleTypeText(detail.settleType) }}</div>
        </div>
        <div class="field" v-if="detail?.settleFeeRate !== undefined">
          <div class="field-label">结算费率</div>
          <div class="field-value amount-em">{{ fmtRate(detail.settleFeeRate) }}</div>
        </div>
        <div class="field" v-if="detail?.dailyLimit !== undefined">
          <div class="field-label">单日限额</div>
          <div class="field-value">{{ fmtAmount(detail.dailyLimit) }}</div>
        </div>
        <div class="field" v-if="detail?.singleLimit !== undefined">
          <div class="field-label">单笔限额</div>
          <div class="field-value">{{ fmtAmount(detail.singleLimit) }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.notifyUrl">
          <div class="field-label">回调地址</div>
          <div class="field-value">{{ detail.notifyUrl }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.whiteIpList">
          <div class="field-label">IP 白名单</div>
          <div class="field-value">{{ detail.whiteIpList }}</div>
        </div>
        <div class="field field-wide" v-if="detail?.remark">
          <div class="field-label">备注</div>
          <div class="field-value">{{ detail.remark }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">资金账户</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.accountNo">
          <div class="field-label">账户编号</div>
          <div class="field-value num">{{ detail.accountNo }}</div>
        </div>
        <div class="field">
          <div class="field-label">账户余额</div>
          <div class="field-value amount-em">{{ fmtAmount(detail?.balance) }}</div>
        </div>
        <div class="field">
          <div class="field-label">冻结金额</div>
          <div class="field-value">{{ fmtAmount(detail?.frozenAmount) }}</div>
        </div>
        <div class="field">
          <div class="field-label">可用余额</div>
          <div class="field-value amount-em">{{ fmtAmount(availableBalance) }}</div>
        </div>
      </div>
    </div>

    <div class="group mt-10px">
      <div class="group-title">密钥管理</div>
      <div class="info-grid">
        <div class="field" v-if="detail?.appKey">
          <div class="field-label">appKey</div>
          <div class="field-value num">
            {{ detail.appKey }}
            <BaseButton size="small" type="primary" link @click="copyText(detail.appKey)">
              复制
            </BaseButton>
          </div>
        </div>
        <div class="field" v-if="detail?.secretVersion !== undefined">
          <div class="field-label">密钥版本</div>
          <div class="field-value">v{{ detail.secretVersion }}</div>
        </div>
        <div class="field field-wide">
          <div class="field-label">密钥轮换</div>
          <div class="field-value">
            <BaseButton size="small" type="warning" :loading="rotating" @click="handleRotate">
              轮换密钥
            </BaseButton>
            <span class="field-hint">轮换后旧 appSecret 立即失效，新密钥仅展示一次</span>
          </div>
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

    <!-- 新密钥展示：仅此一次，关闭后不可再查看 -->
    <Dialog v-model="secretVisible" title="新密钥已生成" width="600px">
      <div class="secret-alert">{{ secretTip || '新 appSecret 仅此一次展示，请立即保存。' }}</div>
      <div class="secret-box">
        <span class="secret-value">{{ newSecret }}</span>
      </div>
      <div class="secret-version">新密钥版本：v{{ newSecretVersion }}</div>
      <template #footer>
        <ElButton type="primary" @click="copyText(newSecret)">复制密钥</ElButton>
        <ElButton @click="secretVisible = false">我已保存</ElButton>
      </template>
    </Dialog>
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

  .detail-actions {
    margin-left: auto;
  }
}

/* ───────── 结果条：状态 + 名称 + 余额/时间 ───────── */
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

.result-success {
  color: var(--el-color-success);
}

.result-warning {
  color: var(--el-color-warning);
}

.result-info {
  color: var(--el-color-info);
}

.result-money {
  flex: 1;
  margin-left: 48px;

  .money {
    font-size: 26px;
    font-weight: 700;
    line-height: 1.2;
    color: var(--el-text-color-primary);
    word-break: break-all;
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

.field-wide {
  grid-column: span 2;
}

.field-hint {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.num {
  font-weight: 600;
}

.amount-em {
  font-weight: 700;
}

/* ───────── 新密钥弹窗 ───────── */
.secret-alert {
  padding: 10px 14px;
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-radius: 6px;
}

.secret-box {
  padding: 16px;
  background: var(--el-fill-color-light);
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  text-align: center;

  .secret-value {
    font-family: Consolas, Monaco, monospace;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
    word-break: break-all;
    user-select: all;
  }
}

.secret-version {
  margin-top: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
