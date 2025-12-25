<template>
  <div class="views-system-menu-index">
    <el-card>
      <template #header>
        <span>Menu</span>
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
      title: 'Menu'
    }
  }
}
</script>

<style scoped lang="less">
.views-system-menu-index {
  padding: 20px;
}
</style>
