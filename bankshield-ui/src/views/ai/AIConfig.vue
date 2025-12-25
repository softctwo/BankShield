<template>
  <div class="aiconfig-container">
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
  name: 'AIConfig',
  data() {
    return {
      title: 'Ai'
    }
  }
}
</script>

<style scoped lang="less">
.aiconfig-container {
  padding: 20px;
}
</style>
