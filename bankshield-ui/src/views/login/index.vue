<template>
  <div class="views-login-index">
    <el-card>
      <template #header>
        <span>Login</span>
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
      title: 'Login'
    }
  }
}
</script>

<style scoped lang="less">
.views-login-index {
  padding: 20px;
}
</style>
