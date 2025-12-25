<template>
  <div class="views-compliance-check-index">
    <el-card>
      <template #header>
        <span>Check</span>
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
      title: 'Check'
    }
  }
}
</script>

<style scoped lang="less">
.views-compliance-check-index {
  padding: 20px;
}
</style>
