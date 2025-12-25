<template>
  <div class="riskdetaildialog-container">
    <el-card>
      <template #header>
        <span>Components</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'RiskDetailDialog',
  data() {
    return {
      title: 'Components'
    }
  }
}
</script>

<style scoped lang="less">
.riskdetaildialog-container {
  padding: 20px;
}
</style>
