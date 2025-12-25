<template>
  <div class="threatintelligence-container">
    <el-card>
      <template #header>
        <span>Ai</span>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ThreatIntelligence',
  data() {
    return {
      title: 'Ai'
    }
  }
}
</script>

<style scoped lang="less">
.threatintelligence-container {
  padding: 20px;
}
</style>
