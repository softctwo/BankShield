<template>
  <div class="index-container">
    <el-card>
      <template #header>
        <span>Scan Task</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'index',
  data() {
    return {
      title: 'Scan Task'
    }
  }
}
</script>

<style scoped lang="less">
.index-container {
  padding: 20px;
}
</style>
