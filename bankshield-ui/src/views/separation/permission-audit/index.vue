<template>
  <div class="views-separation-permission-audit-index">
    <el-card>
      <template #header>
        <span>Permission Audit</span>
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
      title: 'Permission Audit'
    }
  }
}
</script>

<style scoped lang="less">
.views-separation-permission-audit-index {
  padding: 20px;
}
</style>
