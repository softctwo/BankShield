<template>
  <div class="views-lineage-impact-ImpactAnalysis">
    <el-card>
      <template #header>
        <span>Impact</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Impactanalysis',
  data() {
    return {
      title: 'Impact'
    }
  }
}
</script>

<style scoped lang="less">
.views-lineage-impact-ImpactAnalysis {
  padding: 20px;
}
</style>
