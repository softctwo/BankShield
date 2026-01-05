<template>
  <div class="ip-control-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- IP白名单 -->
      <el-tab-pane label="IP白名单" name="whitelist">
        <el-button type="primary" :icon="Plus" @click="handleAddWhitelist" style="margin-bottom: 15px">
          添加白名单
        </el-button>

        <el-table :data="whitelistData" border stripe>
          <el-table-column prop="ipAddress" label="IP地址" width="150" />
          <el-table-column prop="ipRange" label="IP范围(CIDR)" width="180" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="applyTo" label="应用范围" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.applyTo === 'ALL'" type="success">全局</el-tag>
              <el-tag v-else-if="row.applyTo === 'ROLE'" type="warning">角色</el-tag>
              <el-tag v-else type="info">用户</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'ENABLED'" type="success">启用</el-tag>
              <el-tag v-else type="info">禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" :icon="Delete" @click="handleRemoveWhitelist(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- IP黑名单 -->
      <el-tab-pane label="IP黑名单" name="blacklist">
        <el-button type="danger" :icon="Plus" @click="handleAddBlacklist" style="margin-bottom: 15px">
          添加黑名单
        </el-button>

        <el-table :data="blacklistData" border stripe>
          <el-table-column prop="ipAddress" label="IP地址" width="150" />
          <el-table-column prop="ipRange" label="IP范围(CIDR)" width="180" />
          <el-table-column prop="blockReason" label="封禁原因" show-overflow-tooltip />
          <el-table-column prop="blockType" label="封禁类型" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.blockType === 'MANUAL'" type="warning">手动</el-tag>
              <el-tag v-else type="danger">自动</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="severity" label="严重程度" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.severity === 'LOW'" type="info">低</el-tag>
              <el-tag v-else-if="row.severity === 'MEDIUM'" type="warning">中</el-tag>
              <el-tag v-else-if="row.severity === 'HIGH'" type="danger">高</el-tag>
              <el-tag v-else type="danger">严重</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="expireTime" label="过期时间" width="180">
            <template #default="{ row }">
              {{ row.expireTime || '永久' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'ACTIVE'" type="danger">生效中</el-tag>
              <el-tag v-else-if="row.status === 'EXPIRED'" type="info">已过期</el-tag>
              <el-tag v-else type="success">已移除</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'ACTIVE'"
                link
                type="primary"
                :icon="Delete"
                @click="handleRemoveBlacklist(row)"
              >
                解封
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- IP检查 -->
      <el-tab-pane label="IP检查" name="check">
        <el-card>
          <el-form :model="checkForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="IP地址">
              <el-input v-model="checkForm.ipAddress" placeholder="请输入IP地址" />
            </el-form-item>
            <el-form-item label="用户ID">
              <el-input v-model="checkForm.userId" placeholder="可选，检查用户级白名单" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCheckIp">检查</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div v-if="checkResult" class="check-result">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="IP地址">
                {{ checkResult.ipAddress }}
              </el-descriptions-item>
              <el-descriptions-item label="白名单状态">
                <el-tag v-if="checkResult.whitelisted" type="success">在白名单中</el-tag>
                <el-tag v-else type="info">不在白名单中</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="黑名单状态">
                <el-tag v-if="checkResult.blacklisted" type="danger">已被封禁</el-tag>
                <el-tag v-else type="success">未被封禁</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="访问权限">
                <el-tag v-if="checkResult.allowed" type="success" size="large">
                  <el-icon><Check /></el-icon> 允许访问
                </el-tag>
                <el-tag v-else type="danger" size="large">
                  <el-icon><Close /></el-icon> 拒绝访问
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加白名单对话框 -->
    <el-dialog v-model="whitelistDialogVisible" title="添加IP白名单" width="500px">
      <el-form :model="whitelistForm" label-width="120px">
        <el-form-item label="IP地址" required>
          <el-input v-model="whitelistForm.ipAddress" placeholder="例如: 192.168.1.100" />
        </el-form-item>
        <el-form-item label="IP范围(CIDR)">
          <el-input v-model="whitelistForm.ipRange" placeholder="例如: 192.168.1.0/24" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="whitelistForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="应用范围" required>
          <el-select v-model="whitelistForm.applyTo">
            <el-option label="全局" value="ALL" />
            <el-option label="角色" value="ROLE" />
            <el-option label="用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="whitelistForm.applyTo !== 'ALL'" label="目标ID">
          <el-input v-model="whitelistForm.targetId" placeholder="角色ID或用户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="whitelistDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitWhitelist">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加黑名单对话框 -->
    <el-dialog v-model="blacklistDialogVisible" title="添加IP黑名单" width="500px">
      <el-form :model="blacklistForm" label-width="120px">
        <el-form-item label="IP地址" required>
          <el-input v-model="blacklistForm.ipAddress" placeholder="例如: 192.168.1.100" />
        </el-form-item>
        <el-form-item label="IP范围(CIDR)">
          <el-input v-model="blacklistForm.ipRange" placeholder="例如: 192.168.1.0/24" />
        </el-form-item>
        <el-form-item label="封禁原因" required>
          <el-input v-model="blacklistForm.blockReason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封禁类型" required>
          <el-select v-model="blacklistForm.blockType">
            <el-option label="手动封禁" value="MANUAL" />
            <el-option label="自动封禁" value="AUTO" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" required>
          <el-select v-model="blacklistForm.severity">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="严重" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="blacklistForm.expireTime"
            type="datetime"
            placeholder="留空表示永久封禁"
            format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blacklistDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleSubmitBlacklist">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Check, Close } from '@element-plus/icons-vue'
import {
  addIpWhitelist,
  removeIpWhitelist,
  addIpBlacklist,
  removeIpBlacklist,
  checkIpStatus,
  type IpWhitelist,
  type IpBlacklist
} from '@/api/access-control'

const activeTab = ref('whitelist')
const whitelistData = ref<IpWhitelist[]>([])
const blacklistData = ref<IpBlacklist[]>([])
const whitelistDialogVisible = ref(false)
const blacklistDialogVisible = ref(false)

const whitelistForm = reactive<IpWhitelist>({
  ipAddress: '',
  ipRange: '',
  description: '',
  applyTo: 'ALL',
  targetId: undefined,
  status: 'ENABLED'
})

const blacklistForm = reactive<IpBlacklist>({
  ipAddress: '',
  ipRange: '',
  blockReason: '',
  blockType: 'MANUAL',
  severity: 'MEDIUM',
  expireTime: '',
  status: 'ACTIVE'
})

const checkForm = reactive({
  ipAddress: '',
  userId: ''
})

const checkResult = ref<any>(null)

onMounted(() => {
  loadWhitelist()
  loadBlacklist()
})

const loadWhitelist = () => {
  whitelistData.value = [
    {
      id: 1,
      ipAddress: '10.0.0.0',
      ipRange: '10.0.0.0/8',
      description: '内网A类地址',
      applyTo: 'ALL',
      status: 'ENABLED',
      createdTime: '2025-01-04 10:00:00'
    }
  ]
}

const loadBlacklist = () => {
  blacklistData.value = []
}

const handleAddWhitelist = () => {
  whitelistDialogVisible.value = true
}

const handleSubmitWhitelist = async () => {
  if (!whitelistForm.ipAddress) {
    ElMessage.warning('请输入IP地址')
    return
  }

  try {
    await addIpWhitelist(whitelistForm)
    ElMessage.success('添加白名单成功')
    whitelistDialogVisible.value = false
    loadWhitelist()
  } catch (error) {
    ElMessage.error('添加白名单失败')
  }
}

const handleRemoveWhitelist = async (row: IpWhitelist) => {
  try {
    await ElMessageBox.confirm('确定要删除该白名单吗？', '提示', {
      type: 'warning'
    })
    await removeIpWhitelist(row.id!)
    ElMessage.success('删除成功')
    loadWhitelist()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAddBlacklist = () => {
  blacklistDialogVisible.value = true
}

const handleSubmitBlacklist = async () => {
  if (!blacklistForm.ipAddress || !blacklistForm.blockReason) {
    ElMessage.warning('请输入IP地址和封禁原因')
    return
  }

  try {
    await addIpBlacklist(blacklistForm)
    ElMessage.success('添加黑名单成功')
    blacklistDialogVisible.value = false
    loadBlacklist()
  } catch (error) {
    ElMessage.error('添加黑名单失败')
  }
}

const handleRemoveBlacklist = async (row: IpBlacklist) => {
  try {
    await ElMessageBox.confirm('确定要解封该IP吗？', '提示', {
      type: 'warning'
    })
    await removeIpBlacklist(row.id!)
    ElMessage.success('解封成功')
    loadBlacklist()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('解封失败')
    }
  }
}

const handleCheckIp = async () => {
  if (!checkForm.ipAddress) {
    ElMessage.warning('请输入IP地址')
    return
  }

  try {
    const res = await checkIpStatus(checkForm.ipAddress, checkForm.userId ? Number(checkForm.userId) : undefined)
    if (res.code === 200) {
      checkResult.value = {
        ipAddress: checkForm.ipAddress,
        ...res.data
      }
    }
  } catch (error) {
    ElMessage.error('检查IP状态失败')
  }
}
</script>

<style scoped lang="less">
.ip-control-container {
  padding: 20px;

  .check-result {
    margin-top: 20px;
  }
}
</style>
