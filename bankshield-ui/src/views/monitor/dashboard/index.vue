<template>
  <div class="views-monitor-dashboard-index">
    <el-card>
      <template #header>
        <span>Dashboard</span>
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
      title: 'Dashboard'
    }
  }
}
</script>

<style scoped lang="less">
.views-monitor-dashboard-index {
  padding: 20px;
}
</style>
