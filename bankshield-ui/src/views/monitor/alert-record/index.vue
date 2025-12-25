<template>
  <div class="views-monitor-alert-record-index">
    <el-card>
      <template #header>
        <span>Alert Record</span>
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
      title: 'Alert Record'
    }
  }
}
</script>

<style scoped lang="less">
.views-monitor-alert-record-index {
  padding: 20px;
}
</style>
