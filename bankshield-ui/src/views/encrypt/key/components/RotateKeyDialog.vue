<template>
  <div class="rotatekeydialog-container">
    <el-card>
      <template #header>
        <span>Components</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'RotateKeyDialog',
  data() {
    return {
      title: 'Components'
    }
  }
}
</script>

<style scoped lang="less">
.rotatekeydialog-container {
  padding: 20px;
}
</style>
