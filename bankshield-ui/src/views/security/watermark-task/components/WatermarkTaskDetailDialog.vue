<template>
  <div class="watermarktaskdetaildialog-container">
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
  name: 'WatermarkTaskDetailDialog',
  data() {
    return {
      title: 'Components'
    }
  }
}
</script>

<style scoped lang="less">
.watermarktaskdetaildialog-container {
  padding: 20px;
}
</style>
