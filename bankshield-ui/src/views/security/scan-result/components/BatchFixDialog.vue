<template>
  <el-dialog
    v-model="visible"
    title="批量修复"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="formData" label-width="100px">
      <el-form-item label="修复方式">
        <el-select v-model="formData.fixType" placeholder="请选择修复方式" style="width: 100%">
          <el-option label="自动修复" value="AUTO" />
          <el-option label="手动修复" value="MANUAL" />
          <el-option label="忽略" value="IGNORE" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
  visible: boolean
  selectedItems?: any[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: any): void
}>()

const loading = ref(false)
const formData = reactive({
  fixType: 'AUTO',
  remark: ''
})

const visible = ref(false)

watch(() => props.visible, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:visible', val)
})

const handleClose = () => {
  visible.value = false
  formData.fixType = 'AUTO'
  formData.remark = ''
}

const handleSubmit = async () => {
  loading.value = true
  try {
    emit('submit', {
      fixType: formData.fixType,
      remark: formData.remark,
      ids: props.selectedItems?.map(item => item.id) || []
    })
    ElMessage.success('批量修复任务已提交')
    handleClose()
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}
</script>
