<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { BaseButton } from '@/components/Button'
import { applyMerchantApi } from '@/api/merchant'
import {
  ElButton,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect
} from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({
  name: 'MerchantCreate'
})

const { push } = useRouter()

// 结算方式（对应后端 settleType 1-4，不传默认 T+1）
const settleTypeOptions = [
  { value: 1, label: 'T+1（次日结算）' },
  { value: 2, label: 'T+0（当日结算）' },
  { value: 3, label: '周结' },
  { value: 4, label: '月结' }
]

const form = reactive({
  merchantName: '',
  companyName: '',
  businessLicense: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  settleType: 1,
  // 页面按百分比填写（0.6 表示 0.6%），提交时转成后端要的小数 0.006
  settleFeeRatePercent: undefined as number | undefined,
  dailyLimit: undefined as number | undefined,
  singleLimit: undefined as number | undefined,
  notifyUrl: '',
  // 多个 IP 用逗号、空格或换行分隔，提交时转成 JSON 数组字符串
  whiteIpList: '',
  remark: ''
})

const formRef = ref<FormInstance>()

// el-input-number 清空后是 null，统一按空值提示
const requiredNumber = (label: string, min: number, max?: number) => ({
  validator: (_rule: any, value: any, callback: any) => {
    if (value === null || value === undefined || value === '') {
      callback(new Error(`请输入${label}`))
    } else if (Number(value) < min) {
      callback(new Error(`${label}不能小于 ${min}`))
    } else if (max !== undefined && Number(value) > max) {
      callback(new Error(`${label}不能大于 ${max}`))
    } else {
      callback()
    }
  },
  trigger: ['blur', 'change']
})

const splitIpList = (raw: string) =>
  raw
    .split(/[\s,，;；]+/)
    .map((v) => v.trim())
    .filter(Boolean)

// IP 白名单格式校验：留空表示不限制，填了就必须是合法 IPv4，否则后端会静默放行（等于没配）
const ipListValidator = (_rule: any, value: string, callback: any) => {
  const raw = (value || '').trim()
  if (!raw) {
    callback()
    return
  }
  const list = splitIpList(raw)
  const invalid = list.filter(
    (ip) => !/^(\d{1,3}\.){3}\d{1,3}$/.test(ip) || ip.split('.').some((n) => Number(n) > 255)
  )
  if (invalid.length) {
    callback(new Error(`IP 格式不正确：${invalid.join('、')}（示例 192.168.1.1）`))
  } else {
    callback()
  }
}

const rules: FormRules = {
  merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入企业全称', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  settleType: [{ required: true, message: '请选择结算方式', trigger: 'change' }],
  settleFeeRatePercent: [requiredNumber('结算费率', 0.1, 5)],
  dailyLimit: [requiredNumber('单日交易限额', 0.01)],
  singleLimit: [requiredNumber('单笔交易限额', 0.01)],
  notifyUrl: [
    { required: true, message: '请输入回调通知地址', trigger: 'blur' },
    {
      pattern: /^https?:\/\/.+/,
      message: '回调地址需以 http:// 或 https:// 开头',
      trigger: 'blur'
    }
  ],
  whiteIpList: [{ validator: ipListValidator, trigger: 'blur' }]
}

const submitting = ref(false)
const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const ipList = splitIpList(form.whiteIpList)
    const res = await applyMerchantApi({
      merchantName: form.merchantName.trim(),
      companyName: form.companyName.trim(),
      businessLicense: form.businessLicense.trim(),
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      contactEmail: form.contactEmail.trim(),
      settleType: form.settleType,
      settleFeeRate: Number(((form.settleFeeRatePercent as number) / 100).toFixed(6)),
      dailyLimit: form.dailyLimit as number,
      singleLimit: form.singleLimit as number,
      notifyUrl: form.notifyUrl.trim(),
      whiteIpList: ipList.length ? JSON.stringify(ipList) : undefined,
      remark: form.remark.trim() || undefined
    })
    const data = res.data
    result.merchantNo = data?.merchantNo || ''
    result.appKey = data?.appKey || ''
    result.appSecret = data?.appSecret || ''
    result.tip = data?.tip || ''
    resultVisible.value = true
    formRef.value?.resetFields()
  } finally {
    submitting.value = false
  }
}

// 提交结果：商户号 + 密钥（appSecret 仅此一次展示）
const resultVisible = ref(false)
const result = reactive({
  merchantNo: '',
  appKey: '',
  appSecret: '',
  tip: ''
})

const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择复制')
  }
}

// 密钥已展示过，关闭即回到列表查看待审商户
const finish = () => {
  resultVisible.value = false
  push('/merchant/list')
}
</script>

<template>
  <ContentWrap>
    <div class="page-header">
      <BaseButton type="primary" size="small" @click="push('/merchant/list')">返回列表</BaseButton>
      <span class="page-title">新建商户</span>
    </div>

    <div class="page-tip">
      提交后商户为「待审核 + 禁用」状态，须在「商户审核」通过后才会启用并创建资金账户；appSecret
      仅在提交成功时展示一次，请当场交给商户。
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="create-form mt-10px"
    >
      <div class="group-title">企业信息</div>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="商户名称" prop="merchantName">
            <el-input v-model="form.merchantName" placeholder="对外展示的商户简称" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="企业全称" prop="companyName">
            <el-input v-model="form.companyName" placeholder="与营业执照一致" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="营业执照号" prop="businessLicense">
            <el-input v-model="form.businessLicense" placeholder="统一社会信用代码" clearable />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="group-title">联系人</div>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="form.contactName" placeholder="业务对接人" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="11 位手机号" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input v-model="form.contactEmail" placeholder="用于接收平台通知" clearable />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="group-title">结算配置</div>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="结算方式" prop="settleType">
            <el-select v-model="form.settleType" style="width: 100%">
              <el-option
                v-for="item in settleTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结算费率" prop="settleFeeRatePercent">
            <el-input-number
              v-model="form.settleFeeRatePercent"
              :min="0.1"
              :max="5"
              :precision="2"
              :step="0.1"
              :controls="false"
              style="width: 100%"
            />
            <span class="field-hint">单位 %，范围 0.1 ~ 5（如 0.6 表示 0.6%）</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单日交易限额" prop="dailyLimit">
            <el-input-number
              v-model="form.dailyLimit"
              :min="0.01"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
            <span class="field-hint">单位元</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单笔交易限额" prop="singleLimit">
            <el-input-number
              v-model="form.singleLimit"
              :min="0.01"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
            <span class="field-hint">单位元，不得高于单日限额</span>
          </el-form-item>
        </el-col>
      </el-row>

      <div class="group-title">风控与回调</div>
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="回调地址" prop="notifyUrl">
            <el-input
              v-model="form.notifyUrl"
              placeholder="支付结果通知地址，如 https://mall.example.com/payment/callback"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="IP 白名单" prop="whiteIpList">
            <el-input
              v-model="form.whiteIpList"
              type="textarea"
              :rows="2"
              placeholder="选填，多个 IP 用逗号或换行分隔；留空表示不限制来源 IP"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="2"
              maxlength="255"
              show-word-limit
              placeholder="选填，如接入渠道、商务对接情况"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="form-actions">
        <BaseButton type="primary" :loading="submitting" @click="submit">提交入驻申请</BaseButton>
        <BaseButton @click="formRef?.resetFields()">重置</BaseButton>
      </div>
    </el-form>

    <!-- 入驻结果：商户号 + 密钥（appSecret 仅此一次，关闭后不可再查看） -->
    <Dialog v-model="resultVisible" title="入驻申请已提交" width="620px">
      <div class="result-alert">{{ result.tip || 'appSecret 仅此一次展示，请立即保存。' }}</div>
      <div class="result-row">
        <span class="result-label">商户号</span>
        <span class="result-value num">{{ result.merchantNo }}</span>
      </div>
      <div class="result-row">
        <span class="result-label">appKey</span>
        <span class="result-value num">{{ result.appKey }}</span>
      </div>
      <div class="secret-box">
        <span class="secret-value">{{ result.appSecret }}</span>
      </div>
      <div class="result-note">
        商户当前为「待审核 + 禁用」，审核通过后才能调用支付接口，请前往「商户审核」处理。
      </div>
      <template #footer>
        <ElButton type="primary" @click="copyText(result.appSecret)">复制密钥</ElButton>
        <ElButton @click="finish">我已保存，去审核</ElButton>
      </template>
    </Dialog>
  </ContentWrap>
</template>

<style lang="less" scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .page-title {
    font-size: 16px;
    font-weight: 600;
  }
}

.page-tip {
  margin-top: 10px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-radius: 6px;
}

.group-title {
  position: relative;
  margin: 18px 0 12px;
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

.field-hint {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-left: 120px;
}

/* ───────── 提交结果弹窗 ───────── */
.result-alert {
  padding: 10px 14px;
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-radius: 6px;
}

.result-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);

  .result-label {
    flex-shrink: 0;
    width: 72px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: right;
  }

  .result-value {
    font-size: 14px;
    color: var(--el-text-color-primary);
    word-break: break-all;
  }
}

.secret-box {
  padding: 16px;
  margin-top: 14px;
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

.result-note {
  margin-top: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.num {
  font-weight: 600;
}
</style>
