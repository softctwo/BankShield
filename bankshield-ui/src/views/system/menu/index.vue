<template>
  <div class="menu-management">
    <!-- 操作栏 -->
    <el-card class="mb-4">
      <el-row :gutter="20">
        <el-col :span="18">
          <el-input v-model="queryForm.menuName" placeholder="菜单名称" clearable style="width: 200px" class="mr-2" />
          <el-select v-model="queryForm.status" placeholder="状态" clearable style="width: 120px" class="mr-2">
            <el-option label="正常" :value="0" />
            <el-option label="停用" :value="1" />
          </el-select>
          <el-button type="primary" @click="loadMenuTree">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="success" @click="handleAdd(null)">
            <el-icon><Plus /></el-icon> 新增菜单
          </el-button>
          <el-button type="info" @click="toggleExpandAll">
            {{ isExpandAll ? '折叠' : '展开' }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 菜单树表格 -->
    <el-card>
      <template #header>
        <span>菜单列表</span>
      </template>
      <el-table
        v-loading="loading"
        :data="menuList"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column prop="path" label="路由地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">新增</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="上级菜单">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                :props="{ value: 'id', label: 'menuName', children: 'children' }"
                value-key="id"
                placeholder="选择上级菜单"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜单类型" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio label="M">目录</el-radio>
                <el-radio label="C">菜单</el-radio>
                <el-radio label="F">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType !== 'F'">
            <el-form-item label="图标">
              <el-input v-model="form.icon" placeholder="请输入图标名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType !== 'F'">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType === 'C'">
            <el-form-item label="组件路径">
              <el-input v-model="form.component" placeholder="如：views/system/user/index" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType !== 'M'">
            <el-form-item label="权限标识">
              <el-input v-model="form.perms" placeholder="如：system:user:list" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">正常</el-radio>
                <el-radio :label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType !== 'F'">
            <el-form-item label="是否显示">
              <el-radio-group v-model="form.visible">
                <el-radio :label="0">显示</el-radio>
                <el-radio :label="1">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isExpandAll = ref(true)
const menuList = ref<any[]>([])
const menuOptions = ref<any[]>([])
const formRef = ref()

const queryForm = reactive({
  menuName: '',
  status: undefined as number | undefined
})

const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  menuType: 'M',
  menuName: '',
  orderNum: 0,
  icon: '',
  path: '',
  component: '',
  perms: '',
  status: 0,
  visible: 0
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  orderNum: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }]
}

const loadMenuTree = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/menu/tree', { params: queryForm })
    if (res.code === 200) {
      menuList.value = res.data || []
      menuOptions.value = [{ id: 0, menuName: '主目录', children: res.data || [] }]
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    // 使用完整的模拟数据
    menuList.value = getDefaultMenuData()
    menuOptions.value = [{ id: 0, menuName: '主目录', children: menuList.value }]
  } finally {
    loading.value = false
  }
}

const getDefaultMenuData = () => {
  return [
    { id: 1, menuName: '数据大屏', icon: 'DataBoard', path: '/dashboard', component: 'views/dashboard/index', menuType: 'C', orderNum: 1, status: 0, visible: 0, parentId: 0 },
    { id: 2, menuName: '系统管理', icon: 'Setting', path: '/system', menuType: 'M', orderNum: 2, status: 0, visible: 0, parentId: 0, children: [
      { id: 21, menuName: '用户管理', icon: 'User', path: '/system/user', component: 'views/system/user/index', menuType: 'C', orderNum: 1, status: 0, parentId: 2 },
      { id: 22, menuName: '角色管理', icon: 'UserFilled', path: '/system/role', component: 'views/system/role/index', menuType: 'C', orderNum: 2, status: 0, parentId: 2 },
      { id: 23, menuName: '部门管理', icon: 'OfficeBuilding', path: '/system/dept', component: 'views/system/dept/index', menuType: 'C', orderNum: 3, status: 0, parentId: 2 },
      { id: 24, menuName: '菜单管理', icon: 'Menu', path: '/system/menu', component: 'views/system/menu/index', menuType: 'C', orderNum: 4, status: 0, parentId: 2 }
    ]},
    { id: 3, menuName: '数据分类', icon: 'Files', path: '/classification', menuType: 'M', orderNum: 3, status: 0, visible: 0, parentId: 0, children: [
      { id: 31, menuName: '敏感数据', icon: 'Warning', path: '/classification/sensitive-data', component: 'views/classification/sensitive-data/index', menuType: 'C', orderNum: 1, status: 0, parentId: 3 },
      { id: 32, menuName: '分类规则', icon: 'Document', path: '/classification/rule-management', component: 'views/classification/rule-management/index', menuType: 'C', orderNum: 2, status: 0, parentId: 3 },
      { id: 33, menuName: '数据源管理', icon: 'Connection', path: '/classification/data-source', component: 'views/classification/data-source/index', menuType: 'C', orderNum: 3, status: 0, parentId: 3 },
      { id: 34, menuName: '资产地图', icon: 'MapLocation', path: '/classification/asset-map', component: 'views/classification/asset-map/index', menuType: 'C', orderNum: 4, status: 0, parentId: 3 }
    ]},
    { id: 4, menuName: '数据加密', icon: 'Lock', path: '/encrypt', menuType: 'M', orderNum: 4, status: 0, visible: 0, parentId: 0, children: [
      { id: 41, menuName: '密钥管理', icon: 'Key', path: '/encrypt/key', component: 'views/encrypt/key/index', menuType: 'C', orderNum: 1, status: 0, parentId: 4 },
      { id: 42, menuName: '加密策略', icon: 'SetUp', path: '/encrypt/strategy', component: 'views/encrypt/strategy/index', menuType: 'C', orderNum: 2, status: 0, parentId: 4 },
      { id: 43, menuName: '加密任务', icon: 'DocumentChecked', path: '/encrypt/task', component: 'views/encrypt/task/index', menuType: 'C', orderNum: 3, status: 0, parentId: 4 }
    ]},
    { id: 5, menuName: '数据脱敏', icon: 'Hide', path: '/desensitization', menuType: 'M', orderNum: 5, status: 0, visible: 0, parentId: 0, children: [
      { id: 51, menuName: '脱敏规则', icon: 'EditPen', path: '/desensitization/rule', component: 'views/desensitization/rule/index', menuType: 'C', orderNum: 1, status: 0, parentId: 5 },
      { id: 52, menuName: '脱敏模板', icon: 'Tickets', path: '/desensitization/template', component: 'views/desensitization/template/index', menuType: 'C', orderNum: 2, status: 0, parentId: 5 },
      { id: 53, menuName: '脱敏日志', icon: 'List', path: '/desensitization/log', component: 'views/desensitization/log/index', menuType: 'C', orderNum: 3, status: 0, parentId: 5 }
    ]},
    { id: 6, menuName: '访问控制', icon: 'Unlock', path: '/access-control', menuType: 'M', orderNum: 6, status: 0, visible: 0, parentId: 0, children: [
      { id: 61, menuName: '策略管理', icon: 'Document', path: '/access-control/policy', component: 'views/access-control/policy/index', menuType: 'C', orderNum: 1, status: 0, parentId: 6 },
      { id: 62, menuName: 'IP白名单', icon: 'Position', path: '/access-control/ip', component: 'views/access-control/ip/index', menuType: 'C', orderNum: 2, status: 0, parentId: 6 },
      { id: 63, menuName: 'MFA配置', icon: 'Iphone', path: '/access-control/mfa', component: 'views/access-control/mfa/index', menuType: 'C', orderNum: 3, status: 0, parentId: 6 }
    ]},
    { id: 7, menuName: '审计追踪', icon: 'View', path: '/audit', menuType: 'M', orderNum: 7, status: 0, visible: 0, parentId: 0, children: [
      { id: 71, menuName: '审计概览', icon: 'DataAnalysis', path: '/audit/dashboard', component: 'views/audit/dashboard/index', menuType: 'C', orderNum: 1, status: 0, parentId: 7 },
      { id: 72, menuName: '操作审计', icon: 'Operation', path: '/audit/operation', component: 'views/audit/operation/index', menuType: 'C', orderNum: 2, status: 0, parentId: 7 },
      { id: 73, menuName: '登录审计', icon: 'User', path: '/audit/login', component: 'views/audit/login/index', menuType: 'C', orderNum: 3, status: 0, parentId: 7 }
    ]},
    { id: 8, menuName: '监控告警', icon: 'Bell', path: '/monitor', menuType: 'M', orderNum: 8, status: 0, visible: 0, parentId: 0, children: [
      { id: 81, menuName: '监控大屏', icon: 'Monitor', path: '/monitor/dashboard', component: 'views/monitor/dashboard/index', menuType: 'C', orderNum: 1, status: 0, parentId: 8 },
      { id: 82, menuName: '告警规则', icon: 'AlarmClock', path: '/monitor/alert-rule', component: 'views/monitor/alert-rule/index', menuType: 'C', orderNum: 2, status: 0, parentId: 8 },
      { id: 83, menuName: '告警记录', icon: 'Message', path: '/monitor/alert-record', component: 'views/monitor/alert-record/index', menuType: 'C', orderNum: 3, status: 0, parentId: 8 }
    ]},
    { id: 9, menuName: '合规管理', icon: 'DocumentChecked', path: '/compliance', menuType: 'M', orderNum: 9, status: 0, visible: 0, parentId: 0, children: [
      { id: 91, menuName: '合规概览', icon: 'DataAnalysis', path: '/compliance/dashboard', component: 'views/compliance/dashboard/index', menuType: 'C', orderNum: 1, status: 0, parentId: 9 },
      { id: 92, menuName: '合规规则', icon: 'Document', path: '/compliance/rule', component: 'views/compliance/rule/index', menuType: 'C', orderNum: 2, status: 0, parentId: 9 },
      { id: 93, menuName: '合规检查', icon: 'Select', path: '/compliance/check', component: 'views/compliance/check/index', menuType: 'C', orderNum: 3, status: 0, parentId: 9 }
    ]},
    { id: 10, menuName: '数据血缘', icon: 'Share', path: '/lineage', menuType: 'M', orderNum: 10, status: 0, visible: 0, parentId: 0, children: [
      { id: 101, menuName: '血缘图谱', icon: 'Connection', path: '/lineage/graph', component: 'views/classification/lineage/index', menuType: 'C', orderNum: 1, status: 0, parentId: 10 }
    ]},
    { id: 11, menuName: '区块链存证', icon: 'Box', path: '/blockchain', menuType: 'M', orderNum: 11, status: 0, visible: 0, parentId: 0, children: [
      { id: 111, menuName: '存证概览', icon: 'DataAnalysis', path: '/blockchain/dashboard', component: 'views/blockchain/Dashboard', menuType: 'C', orderNum: 1, status: 0, parentId: 11 },
      { id: 112, menuName: '存证记录', icon: 'List', path: '/blockchain/records', component: 'views/blockchain/RecordList', menuType: 'C', orderNum: 2, status: 0, parentId: 11 }
    ]},
    { id: 12, menuName: 'AI智能', icon: 'Cpu', path: '/ai', menuType: 'M', orderNum: 12, status: 0, visible: 0, parentId: 0, children: [
      { id: 121, menuName: 'AI概览', icon: 'DataAnalysis', path: '/ai/dashboard', component: 'views/ai/AIDashboard', menuType: 'C', orderNum: 1, status: 0, parentId: 12 },
      { id: 122, menuName: '模型管理', icon: 'Cpu', path: '/ai/model', component: 'views/ai/ModelManagement', menuType: 'C', orderNum: 2, status: 0, parentId: 12 },
      { id: 123, menuName: '行为分析', icon: 'TrendCharts', path: '/ai/behavior', component: 'views/ai/BehaviorAnalysis', menuType: 'C', orderNum: 3, status: 0, parentId: 12 }
    ]},
    { id: 13, menuName: 'MPC计算', icon: 'Connection', path: '/mpc', menuType: 'M', orderNum: 13, status: 0, visible: 0, parentId: 0, children: [
      { id: 131, menuName: 'MPC概览', icon: 'DataAnalysis', path: '/mpc/dashboard', component: 'views/mpc/Dashboard', menuType: 'C', orderNum: 1, status: 0, parentId: 13 },
      { id: 132, menuName: '任务列表', icon: 'List', path: '/mpc/jobs', component: 'views/mpc/JobList', menuType: 'C', orderNum: 2, status: 0, parentId: 13 }
    ]},
    { id: 14, menuName: '联邦学习', icon: 'DataLine', path: '/federated', menuType: 'M', orderNum: 14, status: 0, visible: 0, parentId: 0, children: [
      { id: 141, menuName: '联邦概览', icon: 'DataAnalysis', path: '/federated/dashboard', component: 'views/federated/FederatedDashboard', menuType: 'C', orderNum: 1, status: 0, parentId: 14 }
    ]},
    { id: 15, menuName: '安全防护', icon: 'Shield', path: '/security', menuType: 'M', orderNum: 15, status: 0, visible: 0, parentId: 0, children: [
      { id: 151, menuName: '安全扫描', icon: 'Search', path: '/security/scan-task', component: 'views/security/scan-task/index', menuType: 'C', orderNum: 1, status: 0, parentId: 15 },
      { id: 152, menuName: '扫描结果', icon: 'Document', path: '/security/scan-result', component: 'views/security/scan-result/index', menuType: 'C', orderNum: 2, status: 0, parentId: 15 },
      { id: 153, menuName: '水印模板', icon: 'Stamp', path: '/security/watermark-template', component: 'views/security/watermark-template/index', menuType: 'C', orderNum: 3, status: 0, parentId: 15 },
      { id: 154, menuName: '水印提取', icon: 'View', path: '/security/watermark-extract', component: 'views/security/watermark-extract/index', menuType: 'C', orderNum: 4, status: 0, parentId: 15 }
    ]}
  ]
}

const resetQuery = () => {
  queryForm.menuName = ''
  queryForm.status = undefined
  loadMenuTree()
}

const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  loadMenuTree()
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    parentId: 0,
    menuType: 'M',
    menuName: '',
    orderNum: 0,
    icon: '',
    path: '',
    component: '',
    perms: '',
    status: 0,
    visible: 0
  })
}

const handleAdd = (row: any) => {
  resetForm()
  if (row) {
    form.parentId = row.id
  }
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除菜单 "${row.menuName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/api/menu/${row.id}`)
    ElMessage.success('删除成功')
    loadMenuTree()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    if (form.id) {
      await request.put(`/api/menu/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/api/menu', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadMenuTree()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
    }
  }
}

onMounted(() => {
  loadMenuTree()
})
</script>

<style scoped lang="less">
.menu-management {
  padding: 20px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mr-2 {
  margin-right: 8px;
}
</style>
