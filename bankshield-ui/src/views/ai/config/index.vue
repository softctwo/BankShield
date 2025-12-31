<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">AI配置</h1>
      <p class="mt-1 text-sm text-gray-500">配置AI模型和分析参数</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧配置菜单 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow p-4">
          <div class="space-y-2">
            <button
              v-for="item in configMenu"
              :key="item.key"
              @click="activeConfig = item.key"
              :class="[
                'w-full text-left px-4 py-3 rounded-lg transition-colors',
                activeConfig === item.key
                  ? 'bg-blue-50 text-blue-600 font-medium'
                  : 'text-gray-700 hover:bg-gray-50'
              ]"
            >
              <el-icon class="mr-2">
                <component :is="item.icon" />
              </el-icon>
              {{ item.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧配置内容 -->
      <div class="lg:col-span-2">
        <!-- AI模型配置 -->
        <div v-show="activeConfig === 'model'" class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">AI模型配置</h3>
          <el-form :model="modelConfig" label-width="120px">
            <el-form-item label="AI服务提供商">
              <el-select v-model="modelConfig.provider" placeholder="请选择AI服务提供商" class="w-full">
                <el-option label="OpenAI" value="openai" />
                <el-option label="Azure OpenAI" value="azure" />
                <el-option label="Claude (Anthropic)" value="claude" />
                <el-option label="本地模型" value="local" />
              </el-select>
            </el-form-item>

            <el-form-item label="模型名称">
              <el-select v-model="modelConfig.model" placeholder="请选择模型" class="w-full">
                <el-option v-if="modelConfig.provider === 'openai'" label="GPT-4" value="gpt-4" />
                <el-option v-if="modelConfig.provider === 'openai'" label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
                <el-option v-if="modelConfig.provider === 'claude'" label="Claude 3 Opus" value="claude-3-opus" />
                <el-option v-if="modelConfig.provider === 'claude'" label="Claude 3 Sonnet" value="claude-3-sonnet" />
                <el-option v-if="modelConfig.provider === 'local'" label="本地模型" value="local-model" />
              </el-select>
            </el-form-item>

            <el-form-item label="API密钥">
              <el-input v-model="modelConfig.apiKey" type="password" placeholder="请输入API密钥" show-password />
            </el-form-item>

            <el-form-item label="API端点">
              <el-input v-model="modelConfig.apiEndpoint" placeholder="https://api.openai.com/v1" />
            </el-form-item>

            <el-form-item label="温度参数">
              <el-slider v-model="modelConfig.temperature" :min="0" :max="2" :step="0.1" show-input />
            </el-form-item>

            <el-form-item label="最大Token数">
              <el-input-number v-model="modelConfig.maxTokens" :min="100" :max="4000" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleTestConnection" :loading="testLoading">测试连接</el-button>
              <el-button type="primary" @click="handleSaveModelConfig" :loading="saveLoading">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 分析规则配置 -->
        <div v-show="activeConfig === 'rules'" class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">分析规则配置</h3>
          <el-form :model="rulesConfig" label-width="150px">
            <div class="mb-6">
              <h4 class="text-base font-medium text-gray-900 mb-3">异常检测</h4>
              <el-form-item label="异常检测阈值">
                <el-slider v-model="rulesConfig.anomalyThreshold" :min="0" :max="100" show-input />
              </el-form-item>
              <el-form-item label="检测敏感度">
                <el-radio-group v-model="rulesConfig.sensitivity">
                  <el-radio label="low">低</el-radio>
                  <el-radio label="medium">中</el-radio>
                  <el-radio label="high">高</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="启用实时检测">
                <el-switch v-model="rulesConfig.realtimeDetection" />
              </el-form-item>
            </div>

            <div class="mb-6 pt-6 border-t border-gray-200">
              <h4 class="text-base font-medium text-gray-900 mb-3">威胁识别</h4>
              <el-form-item label="威胁类型">
                <el-checkbox-group v-model="rulesConfig.threatTypes">
                  <el-checkbox label="sql_injection">SQL注入</el-checkbox>
                  <el-checkbox label="xss">XSS攻击</el-checkbox>
                  <el-checkbox label="ddos">DDoS攻击</el-checkbox>
                  <el-checkbox label="data_leak">数据泄露</el-checkbox>
                  <el-checkbox label="privilege_escalation">权限提升</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="威胁评分阈值">
                <el-slider v-model="rulesConfig.threatScoreThreshold" :min="0" :max="100" show-input />
              </el-form-item>
            </div>

            <div class="mb-6 pt-6 border-t border-gray-200">
              <h4 class="text-base font-medium text-gray-900 mb-3">告警触发</h4>
              <el-form-item label="自动触发告警">
                <el-switch v-model="rulesConfig.autoAlert" />
              </el-form-item>
              <el-form-item label="告警级别">
                <el-select v-model="rulesConfig.alertLevel" placeholder="请选择告警级别">
                  <el-option label="低危" value="low" />
                  <el-option label="中危" value="medium" />
                  <el-option label="高危" value="high" />
                  <el-option label="严重" value="critical" />
                </el-select>
              </el-form-item>
              <el-form-item label="通知方式">
                <el-checkbox-group v-model="rulesConfig.notificationMethods">
                  <el-checkbox label="email">邮件</el-checkbox>
                  <el-checkbox label="sms">短信</el-checkbox>
                  <el-checkbox label="webhook">Webhook</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </div>

            <el-form-item>
              <el-button type="primary" @click="handleSaveRulesConfig" :loading="saveLoading">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 训练数据 -->
        <div v-show="activeConfig === 'training'" class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">训练数据管理</h3>
          <div class="space-y-6">
            <div>
              <h4 class="text-base font-medium text-gray-900 mb-3">数据集统计</h4>
              <div class="grid grid-cols-3 gap-4">
                <div class="bg-blue-50 rounded-lg p-4">
                  <p class="text-sm text-blue-600">正常样本</p>
                  <p class="text-2xl font-bold text-blue-900">12,345</p>
                </div>
                <div class="bg-red-50 rounded-lg p-4">
                  <p class="text-sm text-red-600">异常样本</p>
                  <p class="text-2xl font-bold text-red-900">1,234</p>
                </div>
                <div class="bg-green-50 rounded-lg p-4">
                  <p class="text-sm text-green-600">准确率</p>
                  <p class="text-2xl font-bold text-green-900">94.5%</p>
                </div>
              </div>
            </div>

            <div class="pt-6 border-t border-gray-200">
              <h4 class="text-base font-medium text-gray-900 mb-3">上传训练数据</h4>
              <el-upload
                class="upload-demo"
                drag
                action="/api/ai/upload-training-data"
                multiple
                :on-success="handleUploadSuccess"
              >
                <el-icon class="el-icon--upload"><Upload /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持CSV、JSON格式，单个文件不超过10MB
                  </div>
                </template>
              </el-upload>
            </div>

            <div class="pt-6 border-t border-gray-200">
              <h4 class="text-base font-medium text-gray-900 mb-3">模型训练</h4>
              <el-button type="primary" @click="handleStartTraining" :loading="trainingLoading">
                <el-icon class="mr-2"><VideoPlay /></el-icon>
                开始训练
              </el-button>
              <el-button @click="handleStopTraining" :disabled="!trainingLoading">
                <el-icon class="mr-2"><VideoPause /></el-icon>
                停止训练
              </el-button>
            </div>
          </div>
        </div>

        <!-- API调用日志 -->
        <div v-show="activeConfig === 'logs'" class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">API调用日志</h3>
          <div class="overflow-hidden">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">时间</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">接口</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">耗时</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Token消耗</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="log in apiLogs" :key="log.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.time }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ log.api }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span :class="[
                      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                      log.status === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    ]">
                      {{ log.status === 'success' ? '成功' : '失败' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.duration }}ms</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.tokens }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Document, DataAnalysis, List, Upload, VideoPlay, VideoPause } from '@element-plus/icons-vue'

const activeConfig = ref('model')
const saveLoading = ref(false)
const testLoading = ref(false)
const trainingLoading = ref(false)

const configMenu = [
  { key: 'model', label: 'AI模型配置', icon: 'Setting' },
  { key: 'rules', label: '分析规则', icon: 'Document' },
  { key: 'training', label: '训练数据', icon: 'DataAnalysis' },
  { key: 'logs', label: 'API日志', icon: 'List' }
]

const modelConfig = reactive({
  provider: 'openai',
  model: 'gpt-4',
  apiKey: '',
  apiEndpoint: 'https://api.openai.com/v1',
  temperature: 0.7,
  maxTokens: 2000
})

const rulesConfig = reactive({
  anomalyThreshold: 75,
  sensitivity: 'medium',
  realtimeDetection: true,
  threatTypes: ['sql_injection', 'xss', 'ddos', 'data_leak'],
  threatScoreThreshold: 70,
  autoAlert: true,
  alertLevel: 'high',
  notificationMethods: ['email', 'webhook']
})

const apiLogs = ref([
  { id: 1, time: '2024-12-30 13:45:00', api: '/api/ai/analyze', status: 'success', duration: 1250, tokens: 850 },
  { id: 2, time: '2024-12-30 13:40:00', api: '/api/ai/detect', status: 'success', duration: 980, tokens: 620 },
  { id: 3, time: '2024-12-30 13:35:00', api: '/api/ai/predict', status: 'success', duration: 1500, tokens: 1200 },
  { id: 4, time: '2024-12-30 13:30:00', api: '/api/ai/analyze', status: 'failed', duration: 5000, tokens: 0 }
])

const handleTestConnection = async () => {
  testLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 2000))
  testLoading.value = false
  ElMessage.success('AI服务连接成功')
}

const handleSaveModelConfig = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  saveLoading.value = false
  ElMessage.success('模型配置保存成功')
}

const handleSaveRulesConfig = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  saveLoading.value = false
  ElMessage.success('规则配置保存成功')
}

const handleUploadSuccess = () => {
  ElMessage.success('训练数据上传成功')
}

const handleStartTraining = async () => {
  trainingLoading.value = true
  ElMessage.info('模型训练已开始，预计需要30分钟')
  await new Promise(resolve => setTimeout(resolve, 3000))
  trainingLoading.value = false
  ElMessage.success('模型训练完成')
}

const handleStopTraining = () => {
  trainingLoading.value = false
  ElMessage.warning('模型训练已停止')
}
</script>
