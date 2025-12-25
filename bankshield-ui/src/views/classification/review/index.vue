<template>
  <div class="views-classification-review-index">
    <el-card>
      <template #header>
        <span>Review</span>
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
      title: 'Review'
    }
  }
}
</script>

<style scoped lang="less">
.views-classification-review-index {
  padding: 20px;
}
</style>
