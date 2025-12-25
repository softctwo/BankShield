<template>
  <div class="anomaly-list-container">
    <el-card>
      <template #header>
        <span>异常行为管理</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AnomalyList',
  data() {
    return {
      title: '异常行为管理'
    }
  }
}
</script>

<style scoped lang="less">
.anomaly-list-container {
  padding: 20px;
}
</style>
