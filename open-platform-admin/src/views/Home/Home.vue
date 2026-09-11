<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <Icon icon="vi-ant-design:dashboard-filled" :size="64" color="var(--el-color-primary)" />
        <h1 class="welcome-title">欢迎使用开放服务平台</h1>
        <p class="welcome-desc">Open Platform — 为商户提供支付、账户、资金等综合能力</p>
        <el-divider />
        <el-row :gutter="24" class="feature-cards">
          <el-col :span="8">
            <el-statistic title="支付订单" :value="1024">
              <template #prefix>
                <Icon icon="vi-ep:money" />
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="接入商户" :value="86">
              <template #prefix>
                <Icon icon="vi-ep:office-building" />
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平台用户" :value="5680">
              <template #prefix>
                <Icon icon="vi-ep:user-filled" />
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElCard, ElCol, ElDivider, ElRow, ElStatistic } from 'element-plus'

// 首页 — 后续根据需要扩展
</script>

<style lang="less" scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 24px;
}

.welcome-card {
  width: 100%;
  max-width: 900px;

  .welcome-content {
    text-align: center;
    padding: 40px 20px;

    .welcome-title {
      font-size: 24px;
      margin: 16px 0 8px;
      color: var(--el-text-color-primary);
    }

    .welcome-desc {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin-bottom: 16px;
    }

    .feature-cards {
      margin-top: 16px;
    }
  }
}
</style>
