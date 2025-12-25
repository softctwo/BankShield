<template>
  <div class="views-system-dept-index">
    <el-card>
      <template #header>
        <span>Dept</span>
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
      title: 'Dept'
    }
  }
}
</script>

<style scoped lang="less">
.views-system-dept-index {
  padding: 20px;
}
</style>
