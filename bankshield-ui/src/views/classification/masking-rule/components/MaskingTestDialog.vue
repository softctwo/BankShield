<template>
  <el-dialog
    v-model="visible"
    title="测试脱敏规则"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose">
    
    <el-form
      ref="testFormRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      v-loading="loading">
      
      <el-descriptions :column="1" border style="margin-bottom: 20px;">
        <el-descriptions-item label="规则名称">{{ rule?.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="敏感数据类型">
          <el-tag type="primary" effect="plain">{{ getSensitiveTypeName(rule?.sensitiveDataType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="脱敏算法">
          <el-tag type="info" effect="light">{{ getAlgorithmName(rule?.maskingAlgorithm) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form-item label="测试数据" prop="testData">
        <el-input 
          v-model="formData.testData" 
          type="textarea"
          :rows="3"
          placeholder="请输入要测试的数据"
          maxlength="200" 
          show-word-limit />
      </el-form-item>

      <el-form-item label="快速选择" v-if="quickOptions.length > 0">
        <el-space>
          <el-button 
            v-for="option in quickOptions" 
            :key="option.value"
            size="small"
            @click="formData.testData = option.value">
            {{ option.label }}
          </el-button>
        </el-space>
      </el-form-item>

      <el-form-item label="测试结果" v-if="result">
        <el-card shadow="never" style="width: 100%">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="原始数据">
              <span style="color: #f56c6c; font-weight: bold;">{{ result.originalData }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="脱敏数据">
              <span style="color: #67c23a; font-weight: bold;">{{ result.maskedData }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleTest" :loading="testLoading">测试</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ResponseData } from '@/utils/request'
import { testMaskingRule, DataMaskingRule, MaskingTestResponse } from '@/api/masking'

interface Props {
  modelValue: boolean
  rule: DataMaskingRule | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const testFormRef = ref<FormInstance>()
const loading = ref(false)
const testLoading = ref(false)

// 表单数据
const formData = reactive({
  testData: ''
})

// 测试结果
const result = ref<MaskingTestResponse | null>(null)

// 表单验证规则
const rules: FormRules = {
  testData: [
    { required: true, message: '请输入测试数据', trigger: 'blur' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 敏感数据类型名称映射
const sensitiveTypeNames = {
  'PHONE': '手机号',
  'ID_CARD': '身份证号',
  'BANK_CARD': '银行卡号',
  'NAME': '姓名',
  'EMAIL': '邮箱',
  'ADDRESS': '地址'
}

// 算法名称映射
const algorithmNames = {
  'PARTIAL_MASK': '部分掩码',
  'FULL_MASK': '完整掩码',
  'HASH': '哈希算法',
  'SYMMETRIC_ENCRYPT': '对称加密',
  'FORMAT_PRESERVING': '格式保留加密'
}

// 快速选择选项
const quickOptions = computed(() => {
  if (!props.rule) return []
  
  const type = props.rule.sensitiveDataType
  const options = []
  
  switch (type) {
    case 'PHONE':
      options.push(
        { label: '标准手机号', value: '13812345678' },
        { label: '联通手机号', value: '18612345678' },
        { label: '电信手机号', value: '18912345678' }
      )
      break
    case 'ID_CARD':
      options.push(
        { label: '标准身份证', value: '110101199003074567' },
        { label: '15位身份证', value: '110101900307456' }
      )
      break
    case 'BANK_CARD':
      options.push(
        { label: '工商银行', value: '6222021234567890123' },
        { label: '建设银行', value: '6227001234567890123' },
        { label: '农业银行', value: '6228481234567890123' }
      )
      break
    case 'NAME':
      options.push(
        { label: '单姓单名', value: '张三' },
        { label: '单姓双名', value: '张三丰' },
        { label: '复姓单名', value: '欧阳锋' },
        { label: '复姓双名', value: '东方不败' }
      )
      break
    case 'EMAIL':
      options.push(
        { label: 'QQ邮箱', value: '123456@qq.com' },
        { label: '163邮箱', value: 'user@163.com' },
        { label: '企业邮箱', value: 'zhangsan@company.com' }
      )
      break
    case 'ADDRESS':
      options.push(
        { label: '完整地址', value: '北京市朝阳区建国路88号SOHO现代城A座1201室' },
        { label: '小区地址', value: '上海市浦东新区张江高科技园区碧波路690号' }
      )
      break
  }
  
  return options
})

// 获取敏感数据类型名称
const getSensitiveTypeName = (type: string | undefined) => {
  return type ? sensitiveTypeNames[type as keyof typeof sensitiveTypeNames] || type : ''
}

// 获取算法名称
const getAlgorithmName = (algorithm: string | undefined) => {
  return algorithm ? algorithmNames[algorithm as keyof typeof algorithmNames] || algorithm : ''
}

// 监听规则变化
watch(() => props.rule, () => {
  // 重置表单和结果
  formData.testData = ''
  result.value = null
}, { immediate: true })

// 测试脱敏
const handleTest = async () => {
  if (!testFormRef.value || !props.rule) return
  
  try {
    await testFormRef.value.validate()
    
    testLoading.value = true
    
    const requestData = {
      testData: formData.testData,
      sensitiveDataType: props.rule.sensitiveDataType,
      maskingAlgorithm: props.rule.maskingAlgorithm,
      algorithmParams: props.rule.algorithmParams
    }
    
    const res = await testMaskingRule(requestData)

    if ((res as any).code === 200) {
      result.value = (res as any).data
      ElMessage.success('测试成功')
    } else {
      ElMessage.error((res as any).message || '测试失败')
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败时不显示错误
      ElMessage.error('测试失败')
    }
  } finally {
    testLoading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  testFormRef.value?.resetFields()
  result.value = null
  formData.testData = ''
}
</script>

<style scoped lang="scss">
.param-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>