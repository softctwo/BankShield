<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">数据脱敏规则</h1>
        <p class="mt-1 text-sm text-gray-500">配置和管理敏感数据脱敏规则</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
        <el-icon class="mr-2"><Plus /></el-icon>
        新增规则
      </button>
    </div>

    <!-- 脱敏规则列表 -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">规则名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数据类型</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">脱敏算法</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">示例</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="rule in ruleList" :key="rule.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ rule.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ rule.dataType }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {{ rule.algorithm }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500">
              <div class="flex items-center gap-2">
                <span class="text-gray-400">{{ rule.original }}</span>
                <el-icon class="text-gray-400"><Right /></el-icon>
                <span class="text-blue-600">{{ rule.masked }}</span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="rule.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'">
                {{ rule.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleTest(rule)" class="text-blue-600 hover:text-blue-900 mr-3">测试</button>
              <button @click="handleEdit(rule)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
              <button @click="handleDelete(rule)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑规则对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="600px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="form.dataType" placeholder="请选择数据类型" class="w-full">
            <el-option label="手机号" value="手机号" />
            <el-option label="身份证号" value="身份证号" />
            <el-option label="银行卡号" value="银行卡号" />
            <el-option label="邮箱" value="邮箱" />
            <el-option label="姓名" value="姓名" />
          </el-select>
        </el-form-item>
        <el-form-item label="脱敏算法">
          <el-select v-model="form.algorithm" placeholder="请选择脱敏算法" class="w-full">
            <el-option label="部分隐藏" value="部分隐藏" />
            <el-option label="完全隐藏" value="完全隐藏" />
            <el-option label="哈希替换" value="哈希替换" />
            <el-option label="随机替换" value="随机替换" />
          </el-select>
        </el-form-item>
        <el-form-item label="脱敏规则">
          <el-input v-model="form.rule" type="textarea" :rows="3" placeholder="例如：保留前3位和后4位，中间用*替换" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="dialogVisible = false" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200">取消</button>
          <button @click="handleSubmit" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Right } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  dataType: '手机号',
  algorithm: '部分隐藏',
  rule: ''
})

const ruleList = ref([
  { id: 1, name: '手机号脱敏', dataType: '手机号', algorithm: '部分隐藏', original: '13812345678', masked: '138****5678', status: 'active' },
  { id: 2, name: '身份证脱敏', dataType: '身份证号', algorithm: '部分隐藏', original: '110101199001011234', masked: '110101********1234', status: 'active' },
  { id: 3, name: '银行卡脱敏', dataType: '银行卡号', algorithm: '部分隐藏', original: '6222021234567890', masked: '6222**********90', status: 'active' },
  { id: 4, name: '邮箱脱敏', dataType: '邮箱', algorithm: '部分隐藏', original: 'user@example.com', masked: 'u***@example.com', status: 'active' }
])

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { name: '', dataType: '手机号', algorithm: '部分隐藏', rule: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleTest = (row: any) => {
  ElMessage.info(`测试脱敏规则: ${row.name}`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除规则 "${row.name}" 吗？`, '提示', { type: 'warning' })
    const index = ruleList.value.findIndex(r => r.id === row.id)
    if (index > -1) {
      ruleList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleSubmit = () => {
  if (isEdit.value) {
    ElMessage.success('编辑成功')
  } else {
    ruleList.value.unshift({
      id: Date.now(),
      ...form,
      original: '示例数据',
      masked: '脱敏后数据',
      status: 'active'
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
}
</script>
