<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <el-icon :size="64" color="var(--el-color-primary)">
          <svg-icon icon="vi-ant-design:dashboard-filled" />
        </el-icon>
        <h1 class="welcome-title">欢迎使用开放服务平台</h1>
        <p class="welcome-desc">Open Platform — 为商户提供支付、账户、资金等综合能力</p>
        <el-divider />
        <el-row :gutter="24" class="feature-cards">
          <el-col :span="8">
            <el-statistic title="支付订单" :value="1024">
              <template #prefix>
                <el-icon><svg-icon icon="vi-ep:money" /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="接入商户" :value="86">
              <template #prefix>
                <el-icon><svg-icon icon="vi-ep:office-building" /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平台用户" :value="5680">
              <template #prefix>
                <el-icon><svg-icon icon="vi-ep:user-filled" /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
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
