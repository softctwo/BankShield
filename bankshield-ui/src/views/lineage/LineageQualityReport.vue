<template>
  <div class="views-lineage-LineageQualityReport">
    <el-card>
      <template #header>
        <span>Lineage</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Lineagequalityreport',
  data() {
    return {
      title: 'Lineage'
    }
  }
}
</script>

<style scoped lang="less">
.views-lineage-LineageQualityReport {
  padding: 20px;
}
</style>
