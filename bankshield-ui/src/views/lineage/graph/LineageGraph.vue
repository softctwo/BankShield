<template>
  <div class="views-lineage-graph-LineageGraph">
    <el-card>
      <template #header>
        <span>Graph</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Lineagegraph',
  data() {
    return {
      title: 'Graph'
    }
  }
}
</script>

<style scoped lang="less">
.views-lineage-graph-LineageGraph {
  padding: 20px;
}
</style>
