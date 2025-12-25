<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose">
    
    <el-form
      ref="ruleFormRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      v-loading="loading">
      
      <el-form-item label="规则名称" prop="ruleName">
        <el-input 
          v-model="formData.ruleName" 
          placeholder="请输入规则名称"
          maxlength="100" 
          show-word-limit />
      </el-form-item>

      <el-form-item label="敏感数据类型" prop="sensitiveDataType">
        <el-select v-model="formData.sensitiveDataType" placeholder="请选择敏感数据类型" style="width: 100%">
          <el-option label="手机号" value="PHONE" />
          <el-option label="身份证号" value="ID_CARD" />
          <el-option label="银行卡号" value="BANK_CARD" />
          <el-option label="姓名" value="NAME" />
          <el-option label="邮箱" value="EMAIL" />
          <el-option label="地址" value="ADDRESS" />
        </el-select>
      </el-form-item>

      <el-form-item label="脱敏算法" prop="maskingAlgorithm">
        <el-select v-model="formData.maskingAlgorithm" placeholder="请选择脱敏算法" style="width: 100%">
          <el-option label="部分掩码" value="PARTIAL_MASK" />
          <el-option label="完整掩码" value="FULL_MASK" />
          <el-option label="哈希算法" value="HASH" />
          <el-option label="对称加密" value="SYMMETRIC_ENCRYPT" />
          <el-option label="格式保留加密" value="FORMAT_PRESERVING" />
        </el-select>
      </el-form-item>

      <!-- 算法参数动态表单 -->
      <el-form-item label="算法参数" v-if="formData.maskingAlgorithm">
        <el-card shadow="never" style="width: 100%">
          <template #header>
            <span>{{ getAlgorithmName(formData.maskingAlgorithm) }}参数配置</span>
          </template>
          
          <!-- 部分掩码参数 -->
          <template v-if="formData.maskingAlgorithm === 'PARTIAL_MASK'">
            <el-form-item label="保留前缀" prop="params.keepPrefix">
              <el-input-number 
                v-model="formData.params.keepPrefix" 
                :min="0" 
                :max="20"
                controls-position="right" />
              <span class="param-tip">保留数据前面的字符数</span>
            </el-form-item>
            <el-form-item label="保留后缀" prop="params.keepSuffix">
              <el-input-number 
                v-model="formData.params.keepSuffix" 
                :min="0" 
                :max="20"
                controls-position="right" />
              <span class="param-tip">保留数据后面的字符数</span>
            </el-form-item>
            <el-form-item label="掩码字符" prop="params.maskChar">
              <el-input 
                v-model="formData.params.maskChar" 
                maxlength="1" 
                placeholder="*"
                style="width: 80px" />
              <span class="param-tip">用于替换的字符，默认为*</span>
            </el-form-item>
          </template>

          <!-- 哈希算法参数 -->
          <template v-if="formData.maskingAlgorithm === 'HASH'">
            <el-form-item label="哈希算法" prop="params.hashAlgorithm">
              <el-select v-model="formData.params.hashAlgorithm" placeholder="请选择哈希算法">
                <el-option label="SM3（国密）" value="SM3" />
                <el-option label="SHA256" value="SHA256" />
              </el-select>
              <span class="param-tip">选择哈希算法类型</span>
            </el-form-item>
          </template>

          <!-- 格式保留加密参数 -->
          <template v-if="formData.maskingAlgorithm === 'FORMAT_PRESERVING'">
            <el-form-item label="保留长度" prop="params.formatPreserveLength">
              <el-input-number 
                v-model="formData.params.formatPreserveLength" 
                :min="2" 
                :max="10"
                controls-position="right" />
              <span class="param-tip">保留的字符总长度</span>
            </el-form-item>
          </template>

          <!-- 完整掩码参数 -->
          <template v-if="formData.maskingAlgorithm === 'FULL_MASK'">
            <el-form-item label="掩码字符" prop="params.maskChar">
              <el-input 
                v-model="formData.params.maskChar" 
                maxlength="1" 
                placeholder="*"
                style="width: 80px" />
              <span class="param-tip">用于替换的字符，默认为*</span>
            </el-form-item>
          </template>
        </el-card>
      </el-form-item>

      <el-form-item label="适用场景" prop="applicableScenarios">
        <el-checkbox-group v-model="formData.applicableScenarios">
          <el-checkbox label="DISPLAY">页面展示</el-checkbox>
          <el-checkbox label="EXPORT">数据导出</el-checkbox>
          <el-checkbox label="QUERY">查询结果</el-checkbox>
          <el-checkbox label="TRANSFER">数据传输</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input 
          v-model="formData.description" 
          type="textarea" 
          :rows="3"
          placeholder="请输入规则描述"
          maxlength="500" 
          show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ResponseData } from '@/utils/request'
import {
  createMaskingRule,
  updateMaskingRule,
  DataMaskingRule
} from '@/api/masking'

interface Props {
  modelValue: boolean
  rule: DataMaskingRule | null
  title: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const ruleFormRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  ruleName: '',
  sensitiveDataType: '',
  maskingAlgorithm: '',
  algorithmParams: '',
  applicableScenarios: [] as string[],
  description: '',
  params: {
    keepPrefix: 3,
    keepSuffix: 4,
    maskChar: '*',
    hashAlgorithm: 'SM3',
    formatPreserveLength: 4
  }
})

// 表单验证规则
const rules: FormRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 100, message: '规则名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  sensitiveDataType: [
    { required: true, message: '请选择敏感数据类型', trigger: 'change' }
  ],
  maskingAlgorithm: [
    { required: true, message: '请选择脱敏算法', trigger: 'change' }
  ],
  applicableScenarios: [
    { required: true, message: '请至少选择一个适用场景', trigger: 'change' }
  ],
  'params.keepPrefix': [
    { required: true, message: '请输入保留前缀长度', trigger: 'blur' }
  ],
  'params.keepSuffix': [
    { required: true, message: '请输入保留后缀长度', trigger: 'blur' }
  ],
  'params.maskChar': [
    { required: true, message: '请输入掩码字符', trigger: 'blur' }
  ],
  'params.hashAlgorithm': [
    { required: true, message: '请选择哈希算法', trigger: 'change' }
  ],
  'params.formatPreserveLength': [
    { required: true, message: '请输入保留长度', trigger: 'blur' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 算法名称映射
const algorithmNames = {
  'PARTIAL_MASK': '部分掩码',
  'FULL_MASK': '完整掩码',
  'HASH': '哈希算法',
  'SYMMETRIC_ENCRYPT': '对称加密',
  'FORMAT_PRESERVING': '格式保留加密'
}

// 获取算法名称
const getAlgorithmName = (algorithm: string) => {
  return algorithmNames[algorithm as keyof typeof algorithmNames] || algorithm
}

// 监听规则变化
watch(() => props.rule, (newRule) => {
  if (newRule) {
    // 编辑模式
    formData.id = newRule.id
    formData.ruleName = newRule.ruleName
    formData.sensitiveDataType = newRule.sensitiveDataType
    formData.maskingAlgorithm = newRule.maskingAlgorithm
    formData.description = newRule.description || ''
    
    // 解析适用场景
    if (newRule.applicableScenarios) {
      formData.applicableScenarios = newRule.applicableScenarios.split(',').map(s => s.trim())
    } else {
      formData.applicableScenarios = []
    }
    
    // 解析算法参数
    if (newRule.algorithmParams) {
      try {
        const params = JSON.parse(newRule.algorithmParams)
        Object.assign(formData.params, params)
      } catch (error) {
        console.error('解析算法参数失败:', error)
      }
    }
  } else {
    // 新增模式，重置表单
    resetForm()
  }
}, { immediate: true })

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.ruleName = ''
  formData.sensitiveDataType = ''
  formData.maskingAlgorithm = ''
  formData.algorithmParams = ''
  formData.applicableScenarios = []
  formData.description = ''
  formData.params = {
    keepPrefix: 3,
    keepSuffix: 4,
    maskChar: '*',
    hashAlgorithm: 'SM3',
    formatPreserveLength: 4
  }
}

// 构建算法参数字符串
const buildAlgorithmParams = () => {
  const { maskingAlgorithm, params } = formData
  
  switch (maskingAlgorithm) {
    case 'PARTIAL_MASK':
      return JSON.stringify({
        keepPrefix: params.keepPrefix,
        keepSuffix: params.keepSuffix,
        maskChar: params.maskChar
      })
    case 'FULL_MASK':
      return JSON.stringify({
        maskChar: params.maskChar
      })
    case 'HASH':
      return JSON.stringify({
        hashAlgorithm: params.hashAlgorithm
      })
    case 'FORMAT_PRESERVING':
      return JSON.stringify({
        formatPreserveLength: params.formatPreserveLength
      })
    default:
      return ''
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!ruleFormRef.value) return
  
  try {
    await ruleFormRef.value.validate()
    
    // 构建请求数据
    const requestData: DataMaskingRule = {
      ruleName: formData.ruleName,
      sensitiveDataType: formData.sensitiveDataType,
      maskingAlgorithm: formData.maskingAlgorithm,
      algorithmParams: buildAlgorithmParams(),
      applicableScenarios: formData.applicableScenarios.join(','),
      description: formData.description
    }
    
    if (formData.id) {
      requestData.id = formData.id
    }
    
    submitLoading.value = true

    const res = formData.id
      ? await updateMaskingRule(requestData)
      : await createMaskingRule(requestData)

    if ((res as any).code === 200) {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error((res as any).message || '操作失败')
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败时不显示错误
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  ruleFormRef.value?.resetFields()
}

// 监听算法变化，重置参数
watch(() => formData.maskingAlgorithm, () => {
  // 重置算法相关参数
  formData.params = {
    keepPrefix: 3,
    keepSuffix: 4,
    maskChar: '*',
    hashAlgorithm: 'SM3',
    formatPreserveLength: 4
  }
})
</script>

<style scoped lang="scss">
.param-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>