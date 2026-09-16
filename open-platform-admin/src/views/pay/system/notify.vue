<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { Dialog } from '@/components/Dialog'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { getNotifyPageApi, retryNotifyApi } from '@/api/notify'
import type { NotifyItem } from '@/api/notify/types'
import type { PageResult } from '@/api/pay/order/types'
import { fmtTime, notifyStatusOptions, notifyStatusTagType, notifyTypeOptions } from '../common'
import { ElDescriptions, ElDescriptionsItem, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { reactive, ref, unref } from 'vue'

defineOptions({
  name: 'NotifyList'
})

const searchParams = ref({})
const setSearchParams = (params: any) => {
  searchParams.value = params
  getList()
}

// 通知状态 0（待通知）是合法筛选值，判空必须用 === '' ，不能用 falsy
const hasValue = (v: any) => v !== undefined && v !== null && v !== ''

const { tableRegister, tableState, tableMethods } = useTable({
  fetchDataApi: async () => {
    const { currentPage, pageSize } = tableState
    const s: any = unref(searchParams)
    const params: Record<string, any> = {
      page: unref(currentPage),
      pageSize: unref(pageSize)
    }
    if (s.paymentNo) params.paymentNo = s.paymentNo
    if (s.merchantNo) params.merchantNo = s.merchantNo
    if (hasValue(s.notifyType)) params.notifyType = s.notifyType
    if (hasValue(s.notifyStatus)) params.notifyStatus = s.notifyStatus
    const res = await getNotifyPageApi(params as any)
    const page = res.data as PageResult<NotifyItem>
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
    field: 'paymentNo',
    label: '支付单号',
    minWidth: 200,
    search: {
      component: 'Input',
      componentProps: { placeholder: '支付单号', clearable: true }
    },
    table: { showOverflowTooltip: true }
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
    minWidth: 120,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'notifyType',
    label: '通知类型',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: notifyTypeOptions,
        clearable: true,
        placeholder: '通知类型'
      }
    },
    table: {
      width: 110,
      align: 'center',
      slots: {
        // 类型是身份不是状态，统一用中性标签；状态语义交给「通知状态」列
        default: (data: any) => (
          <ElTag size="small" effect="plain">
            {data.notifyTypeName}
          </ElTag>
        )
      }
    }
  },
  {
    field: 'notifyStatus',
    label: '通知状态',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: notifyStatusOptions,
        clearable: true,
        placeholder: '通知状态'
      }
    },
    table: {
      width: 110,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag size="small" type={notifyStatusTagType(data.notifyStatus)}>
            {data.notifyStatusName}
          </ElTag>
        )
      }
    }
  },
  {
    field: 'retryCount',
    label: '重试次数',
    search: { hidden: true },
    table: {
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) => (
          <span>
            {data.retryCount} / {data.maxRetry}
          </span>
        )
      }
    }
  },
  {
    field: 'nextRetryTime',
    label: '下次重试时间',
    width: 170,
    search: { hidden: true },
    table: {
      slots: {
        default: (data: any) => <span>{fmtTime(data.nextRetryTime)}</span>
      }
    }
  },
  {
    field: 'lastError',
    label: '最后错误',
    minWidth: 160,
    search: { hidden: true },
    table: {
      showOverflowTooltip: true,
      slots: {
        default: (data: any) => <span>{data.lastError || '-'}</span>
      }
    }
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
    field: 'action',
    label: '操作',
    search: { hidden: true },
    form: { hidden: true },
    detail: { hidden: true },
    table: {
      width: 130,
      align: 'center',
      fixed: 'right',
      slots: {
        default: (data: any) => (
          <>
            <BaseButton size="small" type="primary" link onClick={() => openDetail(data.row)}>
              详情
            </BaseButton>
            {data.row.notifyStatus !== 1 && (
              <BaseButton
                size="small"
                type="warning"
                link
                loading={retryingId.value === data.row.id}
                onClick={() => handleRetry(data.row)}
              >
                重试
              </BaseButton>
            )}
          </>
        )
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)

// ───────── 详情弹窗：请求/响应都是 JSON 报文，能解析就美化 ─────────
const detailVisible = ref(false)
const detailRow = ref<NotifyItem>()

const openDetail = (row: NotifyItem) => {
  detailRow.value = row
  detailVisible.value = true
}

/** 尝试美化 JSON，非 JSON 内容原样返回 */
const pretty = (text?: string | null) => {
  if (!text) return '-'
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch {
    return text
  }
}

const copyText = async (text?: string | null) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('当前浏览器不支持自动复制')
  }
}

// ───────── 手动重试：后端同步发送（最长等 10 秒），成功后重置重试次数 ─────────
const retryingId = ref<number | null>(null)

const handleRetry = async (row: NotifyItem) => {
  try {
    await ElMessageBox.confirm(
      `确认重试支付单 ${row.paymentNo} 的回调通知？将立即向 ${row.notifyUrl} 发送一次，最长等待 10 秒，重试次数与下次重试时间会被重置。`,
      '重试回调通知',
      { confirmButtonText: '确认重试', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    // 用户取消
    return
  }
  retryingId.value = row.id
  try {
    const res = await retryNotifyApi(row.id)
    // 只代表「已尝试发送」，成功与否看刷新后的通知状态（商户没响应时后端仅记录错误）
    if (res.code === 0) {
      ElMessage.success('已发起重试，请查看最新通知状态')
      getList()
    }
  } catch {
    // 网络异常提示已由全局拦截器处理
  } finally {
    retryingId.value = null
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

    <!-- 通知详情弹窗 -->
    <Dialog v-model="detailVisible" title="回调通知详情" width="900px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="支付单号">{{ detailRow.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="通知类型">
            <ElTag size="small" effect="plain">{{ detailRow.notifyTypeName }}</ElTag>
          </el-descriptions-item>
          <el-descriptions-item label="商户号">{{
            detailRow.merchantNo || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="商户名称">
            {{ detailRow.merchantName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="通知状态">
            <ElTag size="small" :type="notifyStatusTagType(detailRow.notifyStatus)">
              {{ detailRow.notifyStatusName }}
            </ElTag>
          </el-descriptions-item>
          <el-descriptions-item label="重试次数">
            {{ detailRow.retryCount }} / {{ detailRow.maxRetry }}
          </el-descriptions-item>
          <el-descriptions-item label="通知地址" :span="2">
            {{ detailRow.notifyUrl }}
          </el-descriptions-item>
          <el-descriptions-item label="下次重试时间">
            {{ fmtTime(detailRow.nextRetryTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ fmtTime(detailRow.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ fmtTime(detailRow.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailRow.lastError" label="最后错误" :span="2">
            <span class="err-text">{{ detailRow.lastError }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="payload-block">
          <div class="payload-head">
            <span>请求报文（平台 → 商户）</span>
            <BaseButton size="small" link type="primary" @click="copyText(detailRow.requestData)">
              复制
            </BaseButton>
          </div>
          <pre class="payload-body">{{ pretty(detailRow.requestData) }}</pre>
        </div>

        <div class="payload-block">
          <div class="payload-head">
            <span>响应报文（商户 → 平台）</span>
            <BaseButton size="small" link type="primary" @click="copyText(detailRow.responseData)">
              复制
            </BaseButton>
          </div>
          <pre class="payload-body">{{ pretty(detailRow.responseData) }}</pre>
        </div>
      </template>
    </Dialog>
  </ContentWrap>
</template>

<style lang="less" scoped>
.payload-block {
  margin-top: 12px;

  .payload-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .payload-body {
    max-height: 220px;
    margin: 0;
    padding: 8px 10px;
    overflow: auto;
    font-family: Consolas, Monaco, monospace;
    font-size: 12px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-all;
    background: var(--el-fill-color-light);
    border-radius: 4px;
  }
}

.err-text {
  color: var(--el-color-danger);
}
</style>
