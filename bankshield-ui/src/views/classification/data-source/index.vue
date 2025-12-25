<template>
  <div class="views-classification-data-source-index">
    <el-card>
      <template #header>
        <span>Data Source</span>
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
      title: 'Data Source'
    }
  }
}
</script>

<style scoped lang="less">
.views-classification-data-source-index {
  padding: 20px;
}
</style>
