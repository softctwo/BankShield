<template>
  <div class="views-compliance-report-template-index">
    <el-card>
      <template #header>
        <span>Report Template</span>
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
      title: 'Report Template'
    }
  }
}
</script>

<style scoped lang="less">
.views-compliance-report-template-index {
  padding: 20px;
}
</style>
