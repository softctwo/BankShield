<template>
  <div class="views-audit-operation-index">
    <el-card>
      <template #header>
        <span>Operation</span>
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
      title: 'Operation'
    }
  }
}
</script>

<style scoped lang="less">
.views-audit-operation-index {
  padding: 20px;
}
</style>
