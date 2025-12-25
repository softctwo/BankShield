<template>
  <el-dialog
    v-model="visible"
    title="创建扫描任务"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose">
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="create-task-form">
      
      <el-form-item label="任务名称" prop="taskName">
        <el-input 
          v-model="form.taskName" 
          placeholder="请输入任务名称"
          maxlength="100"
          show-word-limit />
      </el-form-item>

      <el-form-item label="扫描类型" prop="scanType">
        <el-radio-group v-model="form.scanType">
          <el-radio label="VULNERABILITY">
            <el-icon style="color: #f56c6c;"><Warning /></el-icon>
            漏洞扫描
          </el-radio>
          <el-radio label="CONFIG">
            <el-icon style="color: #e6a23c;"><Tools /></el-icon>
            配置检查
          </el-radio>
          <el-radio label="WEAK_PASSWORD">
            <el-icon style="color: #ff6600;"><Key /></el-icon>
            弱密码检测
          </el-radio>
          <el-radio label="ANOMALY">
            <el-icon style="color: #409eff;"><Warning /></el-icon>
            异常行为检测
          </el-radio>
          <el-radio label="ALL">
            <el-icon style="color: #67c23a;"><CircleCheck /></el-icon>
            全面扫描
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="扫描目标" prop="scanTarget">
        <el-input
          v-model="form.scanTarget"
          type="textarea"
          :rows="3"
          placeholder="请输入扫描目标，多个目标用逗号分隔
例如：http://localhost:8080, http://localhost:3000"
          maxlength="500"
          show-word-limit />
        <template #extra>
          <div class="scan-target-tips">
            <el-icon><InfoFilled /></el-icon>
            提示：支持IP地址、域名、URL格式，多个目标请用英文逗号分隔
          </div>
        </template>
      </el-form-item>

      <el-form-item label="扫描配置" v-if="form.scanType === 'VULNERABILITY'">
        <el-checkbox-group v-model="vulnerabilityChecks">
          <el-checkbox label="sql_injection">SQL注入检测</el-checkbox>
          <el-checkbox label="xss">XSS攻击检测</el-checkbox>
          <el-checkbox label="csrf">CSRF攻击检测</el-checkbox>
          <el-checkbox label="directory_traversal">目录遍历检测</el-checkbox>
          <el-checkbox label="command_injection">命令注入检测</el-checkbox>
          <el-checkbox label="open_ports">开放端口扫描</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="扫描配置" v-if="form.scanType === 'CONFIG'">
        <el-checkbox-group v-model="configChecks">
          <el-checkbox label="password_policy">密码策略检查</el-checkbox>
          <el-checkbox label="session_timeout">会话超时检查</el-checkbox>
          <el-checkbox label="encryption_config">加密配置检查</el-checkbox>
          <el-checkbox label="file_upload">文件上传限制</el-checkbox>
          <el-checkbox label="cors_config">CORS配置检查</el-checkbox>
          <el-checkbox label="sensitive_info">敏感信息泄露检查</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="扫描配置" v-if="form.scanType === 'WEAK_PASSWORD'">
        <el-checkbox-group v-model="weakPasswordChecks">
          <el-checkbox label="common_passwords">常见弱密码字典</el-checkbox>
          <el-checkbox label="system_users">系统用户密码</el-checkbox>
          <el-checkbox label="database_passwords">数据库连接密码</el-checkbox>
          <el-checkbox label="api_keys">API密钥强度</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="扫描配置" v-if="form.scanType === 'ANOMALY'">
        <el-checkbox-group v-model="anomalyChecks">
          <el-checkbox label="abnormal_login">异常登录时间检测</el-checkbox>
          <el-checkbox label="abnormal_ip">异常IP地址检测</el-checkbox>
          <el-checkbox label="high_frequency">高频操作检测</el-checkbox>
          <el-checkbox label="privilege_escalation">权限提升检测</el-checkbox>
          <el-checkbox label="session_anomaly">会话异常检测</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="任务描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          placeholder="请输入任务描述（可选）"
          maxlength="200"
          show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          创建任务
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { SecurityScanTask, ScanType } from '@/types/security-scan'
import { scanTaskApi } from '@/api/security-scan'
import { useUserStore } from '@/stores/user'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive({
  taskName: '',
  scanType: 'VULNERABILITY',
  scanTarget: '',
  description: ''
})

// 扫描配置
const vulnerabilityChecks = ref<string[]>(['sql_injection', 'xss', 'csrf'])
const configChecks = ref<string[]>(['password_policy', 'session_timeout', 'encryption_config'])
const weakPasswordChecks = ref<string[]>(['common_passwords', 'system_users'])
const anomalyChecks = ref<string[]>(['abnormal_login', 'abnormal_ip'])

// 表单验证规则
const rules: FormRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  scanType: [
    { required: true, message: '请选择扫描类型', trigger: 'change' }
  ],
  scanTarget: [
    { required: true, message: '请输入扫描目标', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback(new Error('请输入扫描目标'))
          return
        }
        
        const targets = value.split(',').map(t => t.trim())
        const invalidTargets = targets.filter(target => {
          // 简单的格式验证
          return !target || (!target.match(/^https?:\/\//) && !target.match(/^\d+\.\d+\.\d+\.\d+/) && !target.match(/^[a-zA-Z0-9.-]+$/))
        })
        
        if (invalidTargets.length > 0) {
          callback(new Error('扫描目标格式不正确，请输入有效的IP地址、域名或URL'))
          return
        }
        
        callback()
      },
      trigger: 'blur'
    }
  ]
}

// 监听visible变化，重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

// 方法
const resetForm = () => {
  form.taskName = ''
  form.scanType = 'VULNERABILITY'
  form.scanTarget = ''
  form.description = ''
  vulnerabilityChecks.value = ['sql_injection', 'xss', 'csrf']
  configChecks.value = ['password_policy', 'session_timeout', 'encryption_config']
  weakPasswordChecks.value = ['common_passwords', 'system_users']
  anomalyChecks.value = ['abnormal_login', 'abnormal_ip']
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 构建扫描配置
    const scanConfig = buildScanConfig()
    
    const taskData: Partial<SecurityScanTask> = {
      taskName: form.taskName,
      scanType: form.scanType as ScanType,
      scanTarget: form.scanTarget,
      description: form.description,
      scanConfig: JSON.stringify(scanConfig),
      createdBy: userStore.userInfo?.username || 'unknown'
    }
    
    const response = await scanTaskApi.createTask(taskData)
    
    if (response.data.success) {
      ElMessage.success('扫描任务创建成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.data.message || '创建任务失败')
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('创建任务失败:', error)
      ElMessage.error('创建任务失败')
    }
  } finally {
    submitting.value = false
  }
}

const buildScanConfig = () => {
  const config: Record<string, any> = {
    description: form.description
  }
  
  switch (form.scanType) {
    case 'VULNERABILITY':
      config.checks = vulnerabilityChecks.value
      break
    case 'CONFIG':
      config.checks = configChecks.value
      break
    case 'WEAK_PASSWORD':
      config.checks = weakPasswordChecks.value
      break
    case 'ANOMALY':
      config.checks = anomalyChecks.value
      break
    case 'ALL':
      config.checks = {
        vulnerability: vulnerabilityChecks.value,
        config: configChecks.value,
        weakPassword: weakPasswordChecks.value,
        anomaly: anomalyChecks.value
      }
      break
  }
  
  return config
}
</script>

<style scoped lang="scss">
.create-task-form {
  .el-radio-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .el-checkbox-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .scan-target-tips {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>