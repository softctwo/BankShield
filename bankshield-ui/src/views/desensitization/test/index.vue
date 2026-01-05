<template>
  <div class="desensitization-test-container">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>数据脱敏测试工具</span>
          <el-radio-group v-model="testMode" @change="handleModeChange">
            <el-radio-button value="SINGLE">单条测试</el-radio-button>
            <el-radio-button value="BATCH">批量测试</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-if="testMode === 'SINGLE'" class="single-test">
        <el-form :model="singleForm" label-width="120px">
          <el-form-item label="选择规则">
            <el-select v-model="singleForm.ruleCode" placeholder="请选择脱敏规则" @change="handleRuleChange">
              <el-option
                v-for="rule in ruleList"
                :key="rule.ruleCode"
                :label="rule.ruleName"
                :value="rule.ruleCode"
              >
                <span style="float: left">{{ rule.ruleName }}</span>
                <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                  {{ rule.algorithmType }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="数据类型">
            <el-input :value="currentRule?.dataType" readonly />
          </el-form-item>
          <el-form-item label="算法类型">
            <el-input :value="currentRule?.algorithmType" readonly />
          </el-form-item>
          <el-form-item label="测试数据">
            <el-input
              v-model="singleForm.testData"
              placeholder="请输入测试数据"
              clearable
            />
          </el-form-item>
          <el-form-item label="脱敏结果">
            <el-input
              v-model="singleForm.result"
              readonly
              placeholder="点击测试按钮查看结果"
            >
              <template #append>
                <el-button :icon="CopyDocument" @click="handleCopy(singleForm.result)">复制</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Operation" @click="handleSingleTest">执行测试</el-button>
            <el-button :icon="Refresh" @click="handleClearSingle">清空</el-button>
          </el-form-item>
        </el-form>

        <el-divider content-position="left">快捷测试</el-divider>
        <el-space wrap>
          <el-button @click="quickTest('13812345678', 'PHONE')">手机号测试</el-button>
          <el-button @click="quickTest('110101199001011234', 'ID_CARD')">身份证测试</el-button>
          <el-button @click="quickTest('6222021234567890123', 'BANK_CARD')">银行卡测试</el-button>
          <el-button @click="quickTest('test@example.com', 'EMAIL')">邮箱测试</el-button>
          <el-button @click="quickTest('张三', 'NAME')">姓名测试</el-button>
          <el-button @click="quickTest('北京市朝阳区某某街道123号', 'ADDRESS')">地址测试</el-button>
        </el-space>
      </div>

      <div v-else class="batch-test">
        <el-form :model="batchForm" label-width="120px">
          <el-form-item label="选择规则">
            <el-select v-model="batchForm.ruleCode" placeholder="请选择脱敏规则">
              <el-option
                v-for="rule in ruleList"
                :key="rule.ruleCode"
                :label="rule.ruleName"
                :value="rule.ruleCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="批量数据">
            <el-input
              v-model="batchForm.testData"
              type="textarea"
              :rows="8"
              placeholder="请输入测试数据，每行一条"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Operation" @click="handleBatchTest">批量测试</el-button>
            <el-button :icon="Refresh" @click="handleClearBatch">清空</el-button>
            <el-button :icon="Download" @click="handleExportResult">导出结果</el-button>
          </el-form-item>
        </el-form>

        <el-divider content-position="left">测试结果</el-divider>
        <el-table :data="batchResults" border stripe max-height="400">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="original" label="原始数据" min-width="200" />
          <el-table-column prop="desensitized" label="脱敏结果" min-width="200" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.success ? 'success' : 'danger'">
                {{ row.success ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>使用说明</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="单条测试">
          选择脱敏规则后，输入测试数据，点击"执行测试"按钮查看脱敏结果
        </el-descriptions-item>
        <el-descriptions-item label="批量测试">
          选择脱敏规则后，输入多行测试数据（每行一条），点击"批量测试"按钮查看所有结果
        </el-descriptions-item>
        <el-descriptions-item label="快捷测试">
          提供常见数据类型的示例数据，点击即可快速测试
        </el-descriptions-item>
        <el-descriptions-item label="支持算法">
          MASK（遮盖）、REPLACE（替换）、ENCRYPT（加密）、HASH（哈希）、GENERALIZE（泛化）、SHUFFLE（扰乱）、TRUNCATE（截断）
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Operation, Refresh, CopyDocument, Download, InfoFilled } from '@element-plus/icons-vue'
import * as desensitizationApi from '@/api/desensitization'

interface Rule {
  ruleCode: string
  ruleName: string
  dataType: string
  algorithmType: string
}

interface BatchResult {
  original: string
  desensitized: string
  success: boolean
}

const testMode = ref('SINGLE')
const currentRule = ref<Rule | null>(null)

const singleForm = reactive({
  ruleCode: '',
  testData: '',
  result: ''
})

const batchForm = reactive({
  ruleCode: '',
  testData: ''
})

const ruleList = ref<Rule[]>([])

const loadRules = async () => {
  try {
    const res = await desensitizationApi.getEnabledRules()
    if (res.code === 200) {
      ruleList.value = res.data.map((rule: any) => ({
        ruleCode: rule.ruleCode,
        ruleName: rule.ruleName,
        dataType: rule.dataType,
        algorithmType: rule.algorithmType
      }))
    }
  } catch (error) {
    console.error('加载规则失败', error)
  }
}

onMounted(() => {
  loadRules()
})

const batchResults = ref<BatchResult[]>([])

const handleModeChange = () => {
  handleClearSingle()
  handleClearBatch()
}

const handleRuleChange = (ruleCode: string) => {
  currentRule.value = ruleList.value.find(r => r.ruleCode === ruleCode) || null
}

const handleSingleTest = async () => {
  if (!singleForm.ruleCode) {
    ElMessage.warning('请选择脱敏规则')
    return
  }
  if (!singleForm.testData) {
    ElMessage.warning('请输入测试数据')
    return
  }

  try {
    const res = await desensitizationApi.testSingle(singleForm.ruleCode, singleForm.testData)
    if (res.code === 200) {
      singleForm.result = res.data
      ElMessage.success('测试成功')
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

const handleBatchTest = async () => {
  if (!batchForm.ruleCode) {
    ElMessage.warning('请选择脱敏规则')
    return
  }
  if (!batchForm.testData) {
    ElMessage.warning('请输入测试数据')
    return
  }

  try {
    const lines = batchForm.testData.split('\n').filter(line => line.trim())
    const res = await desensitizationApi.testBatch(batchForm.ruleCode, lines)
    if (res.code === 200) {
      batchResults.value = res.data.map((item: any) => ({
        original: item.original,
        desensitized: item.desensitized,
        success: item.success
      }))
      ElMessage.success(`批量测试完成，共处理 ${lines.length} 条数据`)
    }
  } catch (error) {
    ElMessage.error('批量测试失败')
  }
}

const mockDesensitize = (data: string, type: string): string => {
  const map: Record<string, (s: string) => string> = {
    PHONE: (s) => s.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
    ID_CARD: (s) => s.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2'),
    BANK_CARD: (s) => s.replace(/(\d{4})\d+(\d{4})/, '$1 **** **** $2'),
    EMAIL: (s) => s.replace(/(.{2}).*(@.*)/, '$1***$2'),
    NAME: (s) => s.length > 1 ? s[0] + '*'.repeat(s.length - 1) : s,
    ADDRESS: (s) => s.substring(0, 6) + '***'
  }
  return map[type] ? map[type](data) : data
}

const quickTest = (data: string, type: string) => {
  const rule = ruleList.value.find(r => r.dataType === type)
  if (rule) {
    singleForm.ruleCode = rule.ruleCode
    singleForm.testData = data
    currentRule.value = rule
    handleSingleTest()
  }
}

const handleClearSingle = () => {
  singleForm.ruleCode = ''
  singleForm.testData = ''
  singleForm.result = ''
  currentRule.value = null
}

const handleClearBatch = () => {
  batchForm.ruleCode = ''
  batchForm.testData = ''
  batchResults.value = []
}

const handleCopy = (text: string) => {
  if (!text) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  navigator.clipboard.writeText(text)
  ElMessage.success('复制成功')
}

const handleExportResult = () => {
  if (batchResults.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="less">
.desensitization-test-container {
  padding: 20px;

  .test-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .single-test,
    .batch-test {
      padding: 20px 0;
    }
  }

  .info-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}
</style>
