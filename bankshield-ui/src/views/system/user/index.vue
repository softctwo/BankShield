<template>
  <div class="views-system-user-index">
    <el-card>
      <template #header>
        <span>User</span>
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
      title: 'User'
    }
  }
}
</script>

<style scoped lang="less">
.views-system-user-index {
  padding: 20px;
}
</style>
