<template>
  <div class="views-monitor-metrics-index">
    <el-card>
      <template #header>
        <span>Metrics</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {
      title: 'Metrics'
    }
  }
}
</script>

<style scoped lang="less">
.views-monitor-metrics-index {
  padding: 20px;
}
</style>
