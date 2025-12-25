<template>
  <div class="prediction-container">
    <el-card>
      <template #header>
        <span>Ai</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Prediction',
  data() {
    return {
      title: 'Ai'
    }
  }
}
</script>

<style scoped lang="less">
.prediction-container {
  padding: 20px;
}
</style>
