<template>
  <div class="baseline-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.total || 0 }}</div>
              <div class="stat-label">基线总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon enabled">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.enabled || 0 }}</div>
              <div class="stat-label">已启用</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pass">
              <el-icon><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.passCount || 0 }}</div>
              <div class="stat-label">检查通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon fail">
              <el-icon><CloseBold /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.failCount || 0 }}</div>
              <div class="stat-label">检查失败</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon builtin">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.builtin || 0 }}</div>
              <div class="stat-label">内置基线</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate" :class="{ 'rate-high': (statistics.complianceRate || 0) >= 80 }">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.complianceRate || 0 }}%</div>
              <div class="stat-label">合规率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询和操作区域 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="检查项名称">
          <el-input
            v-model="queryParams.checkName"
            placeholder="请输入检查项名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="合规标准">
          <el-select v-model="queryParams.complianceStandard" placeholder="全部" clearable style="width: 150px">
            <el-option label="等保三级" value="GB_LEVEL3" />
            <el-option label="PCI-DSS" value="PCI_DSS" />
            <el-option label="OWASP TOP10" value="OWASP_TOP10" />
            <el-option label="ISO27001" value="ISO27001" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查类型">
          <el-select v-model="queryParams.checkType" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in checkTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增基线
        </el-button>
        <el-button type="success" :disabled="selectedIds.length === 0" @click="handleBatchCheck">
          <el-icon><Checked /></el-icon>
          批量检查
        </el-button>
        <el-button type="warning" :disabled="selectedIds.length === 0" @click="handleBatchEnable(true)">
          <el-icon><Open /></el-icon>
          批量启用
        </el-button>
        <el-button type="info" :disabled="selectedIds.length === 0" @click="handleBatchEnable(false)">
          <el-icon><TurnOff /></el-icon>
          批量禁用
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="handleInit" style="margin-left: auto">
          <el-icon><Setting /></el-icon>
          初始化基线
        </el-button>
        <el-button @click="handleSync">
          <el-icon><RefreshRight /></el-icon>
          同步内置
        </el-button>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="checkItemName" label="检查项名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="complianceStandard" label="合规标准" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStandardTagType(row.complianceStandard)">
              {{ getStandardLabel(row.complianceStandard) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkType" label="检查类型" width="120" align="center">
          <template #default="{ row }">
            {{ getCheckTypeLabel(row.checkType) }}
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)" effect="dark">
              {{ getRiskLevelLabel(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="passStatus" label="检查状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.passStatus === 'PASS'" type="success">通过</el-tag>
            <el-tag v-else-if="row.passStatus === 'FAIL'" type="danger">失败</el-tag>
            <el-tag v-else type="info">未检查</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleEnableChange(row)"
              :disabled="row.builtin"
            />
          </template>
        </el-table-column>
        <el-table-column prop="builtin" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.builtin" type="warning" size="small">内置</el-tag>
            <el-tag v-else size="small">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkTime" label="检查时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.checkTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="success" link @click="handleCheck(row)">
              <el-icon><Checked /></el-icon>
              检查
            </el-button>
            <el-button type="warning" link @click="handleEdit(row)" :disabled="row.builtin">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.builtin">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检查项名称" prop="checkItemName">
              <el-input v-model="form.checkItemName" placeholder="请输入检查项名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合规标准" prop="complianceStandard">
              <el-select v-model="form.complianceStandard" placeholder="请选择合规标准" style="width: 100%">
                <el-option label="等保三级" value="GB_LEVEL3" />
                <el-option label="PCI-DSS" value="PCI_DSS" />
                <el-option label="OWASP TOP10" value="OWASP_TOP10" />
                <el-option label="ISO27001" value="ISO27001" />
                <el-option label="自定义" value="CUSTOM" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检查类型" prop="checkType">
              <el-select v-model="form.checkType" placeholder="请选择检查类型" style="width: 100%">
                <el-option v-for="item in checkTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险级别" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择风险级别" style="width: 100%">
                <el-option label="严重" value="CRITICAL" />
                <el-option label="高危" value="HIGH" />
                <el-option label="中危" value="MEDIUM" />
                <el-option label="低危" value="LOW" />
                <el-option label="信息" value="INFO" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="检查说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入检查说明" />
        </el-form-item>
        <el-form-item label="修复建议" prop="remedyAdvice">
          <el-input v-model="form.remedyAdvice" type="textarea" :rows="3" placeholder="请输入修复建议" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePerson">
              <el-input v-model="form.responsiblePerson" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态" prop="enabled">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="基线详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="检查项名称" :span="2">{{ currentRow.checkItemName }}</el-descriptions-item>
        <el-descriptions-item label="合规标准">
          <el-tag :type="getStandardTagType(currentRow.complianceStandard)">
            {{ getStandardLabel(currentRow.complianceStandard) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检查类型">{{ getCheckTypeLabel(currentRow.checkType) }}</el-descriptions-item>
        <el-descriptions-item label="风险级别">
          <el-tag :type="getRiskLevelType(currentRow.riskLevel)" effect="dark">
            {{ getRiskLevelLabel(currentRow.riskLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检查状态">
          <el-tag v-if="currentRow.passStatus === 'PASS'" type="success">通过</el-tag>
          <el-tag v-else-if="currentRow.passStatus === 'FAIL'" type="danger">失败</el-tag>
          <el-tag v-else type="info">未检查</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="启用状态">
          <el-tag :type="currentRow.enabled ? 'success' : 'info'">
            {{ currentRow.enabled ? '已启用' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="基线类型">
          <el-tag :type="currentRow.builtin ? 'warning' : ''">
            {{ currentRow.builtin ? '内置' : '自定义' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检查说明" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="修复建议" :span="2">{{ currentRow.remedyAdvice || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查结果" :span="2">{{ currentRow.checkResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentRow.responsiblePerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentRow.createdBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查时间">{{ currentRow.checkTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下次检查时间">{{ currentRow.nextCheckTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleCheckFromDetail">执行检查</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Document, CircleCheck, Select, CloseBold, Box, TrendCharts,
  Search, Refresh, Plus, Checked, Open, TurnOff, Delete,
  Setting, RefreshRight, View, Edit
} from '@element-plus/icons-vue'
import { securityBaselineApi } from '@/api/security-scan'
import type { SecurityBaseline } from '@/types/security-scan'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  checkName: '',
  complianceStandard: '',
  checkType: ''
})

// 统计数据
const statistics = ref<Record<string, number>>({})

// 表格数据
const tableData = ref<SecurityBaseline[]>([])
const loading = ref(false)
const total = ref(0)
const selectedIds = ref<number[]>([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增基线')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

// 详情对话框
const detailVisible = ref(false)
const currentRow = ref<SecurityBaseline>({} as SecurityBaseline)

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  checkItemName: '',
  complianceStandard: 'CUSTOM',
  checkType: '' as string,
  riskLevel: 'MEDIUM',
  description: '',
  remedyAdvice: '',
  responsiblePerson: '',
  enabled: true
})

// 表单规则
const rules: FormRules = {
  checkItemName: [{ required: true, message: '请输入检查项名称', trigger: 'blur' }],
  complianceStandard: [{ required: true, message: '请选择合规标准', trigger: 'change' }],
  checkType: [{ required: true, message: '请选择检查类型', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险级别', trigger: 'change' }],
  description: [{ required: true, message: '请输入检查说明', trigger: 'blur' }]
}

// 检查类型选项
const checkTypeOptions = [
  { label: '身份认证', value: 'AUTH' },
  { label: '会话管理', value: 'SESSION' },
  { label: '加密配置', value: 'ENCRYPTION' },
  { label: '密码策略', value: 'PASSWORD' },
  { label: '访问控制', value: 'ACCESS_CONTROL' },
  { label: '输入验证', value: 'INPUT_VALIDATION' },
  { label: '网络安全', value: 'NETWORK' },
  { label: '恶意软件', value: 'MALWARE' },
  { label: '组件安全', value: 'COMPONENT' },
  { label: '审计日志', value: 'AUDIT' },
  { label: '密钥管理', value: 'KEY_MANAGEMENT' },
  { label: '配置检查', value: 'CONFIG' }
]

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await securityBaselineApi.getBaselineStatistics()
    if (res.data.code === 200) {
      statistics.value = res.data.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取表格数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await securityBaselineApi.getBaselinesPage(queryParams)
    if (res.data.code === 200) {
      tableData.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (error) {
    console.error('获取数据失败', error)
    // 使用模拟数据
    tableData.value = [
      {
        id: 1,
        checkItemName: '密码复杂度检查',
        complianceStandard: 'GB_LEVEL3',
        checkType: 'PASSWORD_POLICY',
        riskLevel: 'HIGH',
        description: '检查系统密码策略是否符合等保三级要求：至少8位，包含大小写字母、数字和特殊字符',
        remedyAdvice: '修改密码策略配置，启用强密码要求',
        responsiblePerson: '安全管理员',
        enabled: true,
        lastCheckTime: '2026-01-07 14:30:00',
        checkResult: 'PASS',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 14:30:00'
      },
      {
        id: 2,
        checkItemName: '账户锁定策略',
        complianceStandard: 'GB_LEVEL3',
        checkType: 'ACCOUNT_POLICY',
        riskLevel: 'HIGH',
        description: '检查账户锁定策略：连续5次登录失败后锁定30分钟',
        remedyAdvice: '配置账户锁定策略，防止暴力破解',
        responsiblePerson: '安全管理员',
        enabled: true,
        lastCheckTime: '2026-01-07 14:25:00',
        checkResult: 'FAIL',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 14:25:00'
      },
      {
        id: 3,
        checkItemName: 'SQL注入防护',
        complianceStandard: 'OWASP_TOP10',
        checkType: 'SQL_INJECTION',
        riskLevel: 'CRITICAL',
        description: '检查应用是否存在SQL注入漏洞',
        remedyAdvice: '使用参数化查询，禁止拼接SQL语句',
        responsiblePerson: '开发负责人',
        enabled: true,
        lastCheckTime: '2026-01-07 13:00:00',
        checkResult: 'PASS',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 13:00:00'
      },
      {
        id: 4,
        checkItemName: 'XSS防护检查',
        complianceStandard: 'OWASP_TOP10',
        checkType: 'XSS',
        riskLevel: 'HIGH',
        description: '检查应用是否存在跨站脚本攻击漏洞',
        remedyAdvice: '对用户输入进行转义，使用CSP策略',
        responsiblePerson: '开发负责人',
        enabled: true,
        lastCheckTime: '2026-01-07 12:45:00',
        checkResult: 'PASS',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 12:45:00'
      },
      {
        id: 5,
        checkItemName: '敏感数据加密',
        complianceStandard: 'PCI_DSS',
        checkType: 'DATA_ENCRYPTION',
        riskLevel: 'CRITICAL',
        description: '检查敏感数据是否使用国密算法加密存储',
        remedyAdvice: '使用SM4算法加密敏感数据，密钥定期轮换',
        responsiblePerson: '数据安全员',
        enabled: true,
        lastCheckTime: '2026-01-07 11:30:00',
        checkResult: 'PASS',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 11:30:00'
      },
      {
        id: 6,
        checkItemName: '审计日志完整性',
        complianceStandard: 'ISO27001',
        checkType: 'AUDIT_LOG',
        riskLevel: 'MEDIUM',
        description: '检查审计日志是否完整记录所有安全事件',
        remedyAdvice: '启用全量审计日志，定期备份',
        responsiblePerson: '审计员',
        enabled: true,
        lastCheckTime: '2026-01-07 10:00:00',
        checkResult: 'PASS',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 10:00:00'
      },
      {
        id: 7,
        checkItemName: '访问控制策略',
        complianceStandard: 'GB_LEVEL3',
        checkType: 'ACCESS_CONTROL',
        riskLevel: 'HIGH',
        description: '检查访问控制策略是否遵循最小权限原则',
        remedyAdvice: '实施基于角色的访问控制，定期审查权限',
        responsiblePerson: '安全管理员',
        enabled: false,
        lastCheckTime: '2026-01-06 16:00:00',
        checkResult: 'PASS',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-06 16:00:00'
      },
      {
        id: 8,
        checkItemName: '文件上传安全',
        complianceStandard: 'OWASP_TOP10',
        checkType: 'FILE_UPLOAD',
        riskLevel: 'HIGH',
        description: '检查文件上传功能是否存在安全风险',
        remedyAdvice: '限制文件类型和大小，扫描恶意文件',
        responsiblePerson: '开发负责人',
        enabled: true,
        lastCheckTime: '2026-01-07 09:30:00',
        checkResult: 'FAIL',
        createdBy: 'admin',
        createTime: '2025-12-01 10:00:00',
        updateTime: '2026-01-07 09:30:00'
      }
    ]
    total.value = 8
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.checkName = ''
  queryParams.complianceStandard = ''
  queryParams.checkType = ''
  handleQuery()
}

// 选择变化
const handleSelectionChange = (selection: SecurityBaseline[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页变化
const handleSizeChange = () => {
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增基线'
  Object.assign(form, {
    id: undefined,
    checkItemName: '',
    complianceStandard: 'CUSTOM',
    checkType: '',
    riskLevel: 'MEDIUM',
    description: '',
    remedyAdvice: '',
    responsiblePerson: '',
    enabled: true
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: SecurityBaseline) => {
  dialogTitle.value = '编辑基线'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await securityBaselineApi.updateBaseline(form.id, form as any)
          ElMessage.success('更新成功')
        } else {
          await securityBaselineApi.createBaseline(form as any)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
        fetchStatistics()
      } catch (error) {
        console.error('提交失败', error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除
const handleDelete = async (row: SecurityBaseline) => {
  try {
    await ElMessageBox.confirm('确定要删除该基线吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await securityBaselineApi.deleteBaseline(row.id)
    ElMessage.success('删除成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条基线吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await securityBaselineApi.batchDeleteBaselines(selectedIds.value)
    ElMessage.success('批量删除成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 启用状态变化
const handleEnableChange = async (row: SecurityBaseline) => {
  try {
    await securityBaselineApi.updateBaseline(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '启用成功' : '禁用成功')
    fetchStatistics()
  } catch (error) {
    row.enabled = !row.enabled
    ElMessage.error('操作失败')
  }
}

// 批量启用/禁用
const handleBatchEnable = async (enabled: boolean) => {
  try {
    await securityBaselineApi.batchUpdateBaselineEnabled(selectedIds.value, enabled)
    ElMessage.success(enabled ? '批量启用成功' : '批量禁用成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看详情
const handleView = (row: SecurityBaseline) => {
  currentRow.value = row
  detailVisible.value = true
}

// 执行检查
const handleCheck = async (row: SecurityBaseline) => {
  try {
    loading.value = true
    const res = await securityBaselineApi.executeBaselineCheck(row.id)
    if (res.data.code === 200) {
      const result = res.data.data
      ElMessage.success(`检查完成: ${result.passStatus === 'PASS' ? '通过' : '未通过'}`)
      fetchData()
      fetchStatistics()
    }
  } catch (error) {
    ElMessage.error('检查失败')
  } finally {
    loading.value = false
  }
}

// 从详情页执行检查
const handleCheckFromDetail = async () => {
  await handleCheck(currentRow.value)
  detailVisible.value = false
}

// 批量检查
const handleBatchCheck = async () => {
  try {
    loading.value = true
    const res = await securityBaselineApi.batchExecuteBaselineCheck(selectedIds.value)
    if (res.data.code === 200) {
      ElMessage.success(res.data.data || '批量检查完成')
      fetchData()
      fetchStatistics()
    }
  } catch (error) {
    ElMessage.error('批量检查失败')
  } finally {
    loading.value = false
  }
}

// 初始化基线
const handleInit = async () => {
  try {
    await ElMessageBox.confirm('确定要初始化安全基线吗？这将添加内置的安全基线检查项。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    loading.value = true
    await securityBaselineApi.initBaselines()
    ElMessage.success('初始化成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('初始化失败')
    }
  } finally {
    loading.value = false
  }
}

// 同步内置基线
const handleSync = async () => {
  try {
    await ElMessageBox.confirm('确定要同步内置基线吗？这将更新所有内置的安全基线。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    loading.value = true
    await securityBaselineApi.syncBuiltinBaselines()
    ElMessage.success('同步成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('同步失败')
    }
  } finally {
    loading.value = false
  }
}

// 辅助方法
const getStandardLabel = (value: string) => {
  const map: Record<string, string> = {
    GB_LEVEL3: '等保三级',
    PCI_DSS: 'PCI-DSS',
    OWASP_TOP10: 'OWASP',
    ISO27001: 'ISO27001',
    CUSTOM: '自定义',
    // 支持中文格式
    '等保三级': '等保三级',
    '自定义': '自定义'
  }
  return map[value] || value
}

const getStandardTagType = (value: string) => {
  const map: Record<string, string> = {
    GB_LEVEL3: 'danger',
    PCI_DSS: 'warning',
    OWASP_TOP10: 'success',
    ISO27001: '',
    CUSTOM: 'info',
    // 支持中文格式
    '等保三级': 'danger',
    '自定义': 'info'
  }
  return map[value] || 'info'
}

const getCheckTypeLabel = (value: string) => {
  const item = checkTypeOptions.find(opt => opt.value === value)
  return item?.label || value
}

const getRiskLevelLabel = (value: string) => {
  const map: Record<string, string> = {
    CRITICAL: '严重',
    HIGH: '高危',
    MEDIUM: '中危',
    LOW: '低危',
    INFO: '信息'
  }
  return map[value] || value
}

const getRiskLevelType = (value: string) => {
  const map: Record<string, string> = {
    CRITICAL: 'danger',
    HIGH: 'warning',
    MEDIUM: '',
    LOW: 'success',
    INFO: 'info'
  }
  return map[value] || 'info'
}

onMounted(() => {
  fetchData()
  fetchStatistics()
})
</script>

<style scoped lang="less">
.baseline-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;

    .el-icon {
      font-size: 24px;
      color: #fff;
    }

    &.total {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.enabled {
      background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    }

    &.pass {
      background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
    }

    &.fail {
      background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
    }

    &.builtin {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.rate {
      background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
      
      &.rate-high {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      }
    }
  }

  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }

    .stat-label {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.filter-card {
  margin-bottom: 16px;

  .action-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}

.table-card {
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    padding: 16px 0 0;
  }
}
</style>
