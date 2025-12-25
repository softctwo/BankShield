<template>
  <div class="views-layout-index">
    <el-card>
      <template #header>
        <span>Layout</span>
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
      title: 'Layout'
    }
  }
}
</script>

<style scoped lang="less">
.views-layout-index {
  padding: 20px;
}
</style>
