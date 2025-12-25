<template>
  <div class="views-classification-asset-map-index">
    <el-card>
      <template #header>
        <span>Asset Map</span>
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
      title: 'Asset Map'
    }
  }
}
</script>

<style scoped lang="less">
.views-classification-asset-map-index {
  padding: 20px;
}
</style>
