<template>
  <div class="index-container">
    <el-card>
      <template #header>
        <span>Blacklist</span>
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
      title: 'Blacklist'
    }
  }
}
</script>

<style scoped lang="less">
.index-container {
  padding: 20px;
}
</style>
