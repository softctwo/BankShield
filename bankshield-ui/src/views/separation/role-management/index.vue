<template>
  <div class="views-separation-role-management-index">
    <el-card>
      <template #header>
        <span>Role Management</span>
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
      title: 'Role Management'
    }
  }
}
</script>

<style scoped lang="less">
.views-separation-role-management-index {
  padding: 20px;
}
</style>
