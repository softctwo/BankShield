<template>
  <div class="views-separation-operation-log-index">
    <el-card>
      <template #header>
        <span>Operation Log</span>
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
      title: 'Operation Log'
    }
  }
}
</script>

<style scoped lang="less">
.views-separation-operation-log-index {
  padding: 20px;
}
</style>
