<template>
  <div class="views-classification-template-index">
    <el-card>
      <template #header>
        <span>Template</span>
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
      title: 'Template'
    }
  }
}
</script>

<style scoped lang="less">
.views-classification-template-index {
  padding: 20px;
}
</style>
