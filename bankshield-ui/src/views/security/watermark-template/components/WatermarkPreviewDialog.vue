<template>
  <div class="watermarkpreviewdialog-container">
    <el-card>
      <template #header>
        <span>Components</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'WatermarkPreviewDialog',
  data() {
    return {
      title: 'Components'
    }
  }
}
</script>

<style scoped lang="less">
.watermarkpreviewdialog-container {
  padding: 20px;
}
</style>
