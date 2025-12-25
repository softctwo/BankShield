<template>
  <div class="views-classification-lineage-index">
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
  name: 'Index',
  data() {
    return {
      title: 'Lineage'
    }
  }
}
</script>

<style scoped lang="less">
.views-classification-lineage-index {
  padding: 20px;
}
</style>
