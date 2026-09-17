<script setup lang="tsx">
import { ContentWrap } from '@/components/ContentWrap'
import { Table } from '@/components/Table'
import { useTable } from '@/hooks/web/useTable'
import { useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import type { CrudSchema } from '@/hooks/web/useCrudSchemas'
import { disableChannelApi, enableChannelApi, getChannelListApi } from '@/api/channel'
import type { ChannelItem } from '@/api/channel/types'
import { fmtTime } from '../common'
import { ElAlert, ElMessage, ElMessageBox, ElSwitch } from 'element-plus'
import { reactive, ref } from 'vue'

defineOptions({
  name: 'ChannelList'
})

const { tableRegister, tableState, tableMethods } = useTable({
  fetchDataApi: async () => {
    const res = await getChannelListApi()
    const list = res.data || []
    return {
      list,
      total: list.length
    }
  }
})
const { loading, dataList } = tableState
const { getList } = tableMethods

getList()

// 本页无搜索/表单，search.hidden 必写：useCrudSchemas 只认 hidden === true，
// 不写会被当成筛选项塞进 searchSchema（当前不渲染 Search 看不出问题，后续加就会冒出幽灵搜索框）
const crudSchemas = reactive<CrudSchema[]>([
  {
    field: 'index',
    label: '序号',
    type: 'index',
    search: { hidden: true },
    table: { width: 60, align: 'center' }
  },
  {
    field: 'channelCode',
    label: '渠道编码',
    width: 160,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'channelName',
    label: '渠道名称',
    minWidth: 160,
    search: { hidden: true },
    table: { showOverflowTooltip: true }
  },
  {
    field: 'status',
    label: '渠道状态',
    width: 180,
    search: { hidden: true },
    table: {
      align: 'center',
      slots: {
        // 开关表达可操作的状态；旁边补文字，避免只靠颜色/位置区分
        default: (data: any) => (
          <div class="status-cell">
            <ElSwitch
              model-value={data.row.status === 1}
              loading={togglingCode.value === data.row.channelCode}
              beforeChange={() => handleToggle(data.row)}
            />
            <span class={data.row.status === 1 ? 'status-on' : 'status-off'}>
              {data.row.statusName}
            </span>
          </div>
        )
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
        default: (data: any) => <span>{fmtTime(data.row.createTime)}</span>
      }
    }
  }
])

const { allSchemas } = useCrudSchemas(crudSchemas)

// ───────── 渠道启停：ElSwitch 的 beforeChange 返回 true 才切换，实际状态以重新拉取的数据为准 ─────────
const togglingCode = ref<string | null>(null)

const handleToggle = async (row: ChannelItem) => {
  const toEnable = row.status !== 1
  if (!toEnable) {
    try {
      await ElMessageBox.confirm(
        `确认停用「${row.channelName}（${row.channelCode}）」？停用后商户用该渠道创建支付订单会被直接拒绝（返回「支付渠道未启用」）。`,
        '停用支付渠道',
        { confirmButtonText: '确认停用', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      // 用户取消
      return false
    }
  }
  togglingCode.value = row.channelCode
  try {
    const res = toEnable
      ? await enableChannelApi(row.channelCode)
      : await disableChannelApi(row.channelCode)
    // 拦截器对业务失败只弹提示不抛异常，必须自己判断 code
    if (res.code !== 0) return false
    ElMessage.success(toEnable ? '渠道已启用' : '渠道已停用')
    await getList()
    return true
  } catch {
    return false
  } finally {
    togglingCode.value = null
  }
}
</script>

<template>
  <ContentWrap>
    <!-- 渠道只有预置三条，无筛选条件，说明性提示代替搜索栏 -->
    <ElAlert type="info" :closable="false" show-icon class="channel-tip">
      <template #title>
        共 3 个预置渠道：BALANCE（余额支付）已接入真实支付链路，ALIPAY / WECHAT
        为预置占位数据、尚未接入。
      </template>
      <template #default>
        停用渠道后，商户用该渠道创建支付订单会被直接拒绝；停用 BALANCE 将导致所有余额支付不可用。
      </template>
    </ElAlert>

    <Table
      :columns="allSchemas.tableColumns"
      :data="dataList"
      :loading="loading"
      @register="tableRegister"
    />
  </ContentWrap>
</template>

<style lang="less" scoped>
.channel-tip {
  margin-bottom: 12px;
}

.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  .status-on {
    color: var(--el-color-success);
  }

  .status-off {
    color: var(--el-text-color-secondary);
  }
}
</style>
