<template>
  <div class="views-compliance-check-history-index">
    <el-card>
      <template #header>
        <span>Check History</span>
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
      title: 'Check History'
    }
  }
}
</script>

<style scoped lang="less">
.views-compliance-check-history-index {
  padding: 20px;
}
</style>
