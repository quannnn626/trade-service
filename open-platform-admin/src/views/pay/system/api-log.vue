<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Search } from '@/components/Search'
import { Table } from '@/components/Table'
import { Dialog } from '@/components/Dialog'
import { BaseButton } from '@/components/Button'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { getApiLogPageApi } from '@/api/log'
import type { ApiLogItem } from '@/api/log/types'
import type { PageResult } from '@/api/pay/order/types'
import { costTagType, fmtTime, signResultOptions } from '../common'
import { ElDescriptions, ElDescriptionsItem, ElMessage, ElTag } from 'element-plus'
import { reactive, ref, unref } from 'vue'

defineOptions({
  name: 'ApiLogList'
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
    if (s.merchantNo) params.merchantNo = s.merchantNo
    if (s.apiName) params.apiName = s.apiName
    if (s.signResult !== undefined && s.signResult !== null && s.signResult !== '') {
      params.signResult = s.signResult
    }
    // 日期范围拆成后端要求的 startTime/endTime
    if (Array.isArray(s.timeRange) && s.timeRange.length === 2) {
      params.startTime = s.timeRange[0]
      params.endTime = s.timeRange[1]
    }
    const res = await getApiLogPageApi(params as any)
    const page = res.data as PageResult<ApiLogItem>
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
    minWidth: 120,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'apiName',
    label: '接口名称',
    width: 140,
    search: {
      component: 'Input',
      componentProps: { placeholder: '接口名（如 pay.create）', clearable: true }
    },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'requestMethod',
    label: '方式',
    search: { hidden: true },
    table: {
      width: 80,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag size="small" type={data.requestMethod === 'POST' ? 'primary' : 'info'}>
            {data.requestMethod}
          </ElTag>
        )
      }
    }
  },
  {
    field: 'requestUrl',
    label: '请求地址',
    minWidth: 200,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'signResult',
    label: '验签结果',
    search: {
      component: 'Select',
      componentProps: {
        style: { width: '100%' },
        options: signResultOptions,
        clearable: true,
        placeholder: '验签结果'
      }
    },
    table: {
      width: 100,
      align: 'center',
      slots: {
        // 表格展示后端回填的名称（数值 code 仅用于筛选）
        default: (data: any) => (
          <ElTag size="small" type={data.signResult === 0 ? 'success' : 'danger'}>
            {data.signResultName}
          </ElTag>
        )
      }
    }
  },
  {
    field: 'costTime',
    label: '耗时',
    search: { hidden: true },
    table: {
      width: 100,
      align: 'center',
      slots: {
        default: (data: any) => (
          <ElTag size="small" effect="plain" type={costTagType(data.costTime)}>
            {data.costTime} ms
          </ElTag>
        )
      }
    }
  },
  {
    field: 'errorMsg',
    label: '错误信息',
    minWidth: 180,
    search: { hidden: true },
    table: {
      showOverflowTooltip: true,
      slots: {
        default: (data: any) => <span>{data.errorMsg || '-'}</span>
      }
    }
  },
  {
    field: 'createTime',
    label: '调用时间',
    width: 170,
    search: { hidden: true },
    table: {
      slots: {
        default: (data: any) => <span>{fmtTime(data.createTime)}</span>
      }
    }
  },
  {
    field: 'timeRange',
    label: '调用时间',
    search: {
      component: 'DatePicker',
      componentProps: {
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: '开始时间',
        endPlaceholder: '结束时间',
        rangeSeparator: '至'
      }
    },
    form: { hidden: true },
    detail: { hidden: true },
    table: { hidden: true }
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
        default: (data: any) => (
          <BaseButton size="small" type="primary" link onClick={() => openDetail(data.row)}>
            详情
          </BaseButton>
        )
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)

// ───────── 日志详情：请求参数/响应结果多为 JSON，能解析就美化、解析不了原样显示 ─────────
const detailVisible = ref(false)
const detailRow = ref<ApiLogItem>()

const openDetail = (row: ApiLogItem) => {
  detailRow.value = row
  detailVisible.value = true
}

/** 尝试美化 JSON，非 JSON 内容原样返回 */
const pretty = (text?: string) => {
  if (!text) return '-'
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch {
    return text
  }
}

const copyText = async (text?: string) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('当前浏览器不支持自动复制')
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

    <!-- 日志详情弹窗 -->
    <Dialog v-model="detailVisible" title="接口日志详情" width="900px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="商户号">{{
            detailRow.merchantNo || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="商户名称">
            {{ detailRow.merchantName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="接口名称">{{ detailRow.apiName }}</el-descriptions-item>
          <el-descriptions-item label="请求方式">
            {{ detailRow.requestMethod }}
          </el-descriptions-item>
          <el-descriptions-item label="请求地址" :span="2">
            {{ detailRow.requestUrl }}
          </el-descriptions-item>
          <el-descriptions-item label="验签结果">
            <ElTag size="small" :type="detailRow.signResult === 0 ? 'success' : 'danger'">
              {{ detailRow.signResultName }}
            </ElTag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            <ElTag size="small" effect="plain" :type="costTagType(detailRow.costTime)">
              {{ detailRow.costTime }} ms
            </ElTag>
          </el-descriptions-item>
          <el-descriptions-item label="调用时间" :span="2">
            {{ fmtTime(detailRow.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailRow.errorMsg" label="错误信息" :span="2">
            <span class="err-text">{{ detailRow.errorMsg }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="payload-block">
          <div class="payload-head">
            <span>请求参数</span>
            <BaseButton size="small" link type="primary" @click="copyText(detailRow.requestParam)">
              复制
            </BaseButton>
          </div>
          <pre class="payload-body">{{ pretty(detailRow.requestParam) }}</pre>
        </div>

        <div class="payload-block">
          <div class="payload-head">
            <span>响应结果</span>
            <BaseButton
              size="small"
              link
              type="primary"
              @click="copyText(detailRow.responseResult)"
            >
              复制
            </BaseButton>
          </div>
          <pre class="payload-body">{{ pretty(detailRow.responseResult) }}</pre>
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
