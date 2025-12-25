<template>
  <div class="views-lineage-quality-DataQuality">
    <el-card>
      <template #header>
        <span>Quality</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Dataquality',
  data() {
    return {
      title: 'Quality'
    }
  }
}
</script>

<style scoped lang="less">
.views-lineage-quality-DataQuality {
  padding: 20px;
}
</style>
