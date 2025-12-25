<template>
  <div class="dashboard-container">
    <el-card>
      <template #header>
        <span>Monitoring</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      title: 'Monitoring'
    }
  }
}
</script>

<style scoped lang="less">
.dashboard-container {
  padding: 20px;
}
</style>
