<template>
  <div class="views-system-role-index">
    <el-card>
      <template #header>
        <span>Role</span>
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
      title: 'Role'
    }
  }
}
</script>

<style scoped lang="less">
.views-system-role-index {
  padding: 20px;
}
</style>
