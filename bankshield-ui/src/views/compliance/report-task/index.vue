<template>
  <div class="views-compliance-report-task-index">
    <el-card>
      <template #header>
        <span>Report Task</span>
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
      title: 'Report Task'
    }
  }
}
</script>

<style scoped lang="less">
.views-compliance-report-task-index {
  padding: 20px;
}
</style>
