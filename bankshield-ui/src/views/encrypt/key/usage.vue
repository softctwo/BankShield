<template>
  <div class="usage-container">
    <el-card>
      <template #header>
        <span>Key</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'usage',
  data() {
    return {
      title: 'Key'
    }
  }
}
</script>

<style scoped lang="less">
.usage-container {
  padding: 20px;
}
</style>
