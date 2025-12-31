<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">系统设置</h1>
      <p class="mt-1 text-sm text-gray-500">配置系统参数和安全策略</p>
    </div>

    <el-tabs v-model="activeTab" class="bg-white rounded-lg shadow">
      <!-- 系统参数 -->
      <el-tab-pane label="系统参数" name="system">
        <div class="p-6">
          <el-form :model="systemConfig" label-width="150px">
            <div class="mb-6">
              <h3 class="text-lg font-medium text-gray-900 mb-4">基本设置</h3>
              <el-form-item label="系统名称">
                <el-input v-model="systemConfig.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              <el-form-item label="系统版本">
                <el-input v-model="systemConfig.version" disabled />
              </el-form-item>
              <el-form-item label="会话超时时间">
                <el-input-number v-model="systemConfig.sessionTimeout" :min="5" :max="120" /> 分钟
              </el-form-item>
              <el-form-item label="文件上传大小限制">
                <el-input-number v-model="systemConfig.maxFileSize" :min="1" :max="100" /> MB
              </el-form-item>
            </div>

            <div class="mb-6 pt-6 border-t border-gray-200">
              <h3 class="text-lg font-medium text-gray-900 mb-4">日志设置</h3>
              <el-form-item label="日志级别">
                <el-select v-model="systemConfig.logLevel" placeholder="请选择日志级别">
                  <el-option label="DEBUG" value="debug" />
                  <el-option label="INFO" value="info" />
                  <el-option label="WARN" value="warn" />
                  <el-option label="ERROR" value="error" />
                </el-select>
              </el-form-item>
              <el-form-item label="日志保留天数">
                <el-input-number v-model="systemConfig.logRetentionDays" :min="7" :max="365" /> 天
              </el-form-item>
            </div>

            <el-form-item>
              <el-button type="primary" @click="handleSaveSystemConfig" :loading="saveLoading">保存配置</el-button>
              <el-button @click="handleResetSystemConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 安全策略 -->
      <el-tab-pane label="安全策略" name="security">
        <div class="p-6">
          <el-form :model="securityConfig" label-width="150px">
            <div class="mb-6">
              <h3 class="text-lg font-medium text-gray-900 mb-4">密码策略</h3>
              <el-form-item label="密码最小长度">
                <el-input-number v-model="securityConfig.passwordMinLength" :min="6" :max="20" />
              </el-form-item>
              <el-form-item label="密码复杂度要求">
                <el-checkbox-group v-model="securityConfig.passwordComplexity">
                  <el-checkbox label="uppercase">包含大写字母</el-checkbox>
                  <el-checkbox label="lowercase">包含小写字母</el-checkbox>
                  <el-checkbox label="number">包含数字</el-checkbox>
                  <el-checkbox label="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="密码有效期">
                <el-input-number v-model="securityConfig.passwordExpireDays" :min="0" :max="365" /> 天 (0表示永不过期)
              </el-form-item>
              <el-form-item label="密码历史记录">
                <el-input-number v-model="securityConfig.passwordHistory" :min="0" :max="10" /> 次
              </el-form-item>
            </div>

            <div class="mb-6 pt-6 border-t border-gray-200">
              <h3 class="text-lg font-medium text-gray-900 mb-4">登录安全</h3>
              <el-form-item label="最大登录失败次数">
                <el-input-number v-model="securityConfig.maxLoginAttempts" :min="3" :max="10" />
              </el-form-item>
              <el-form-item label="账户锁定时间">
                <el-input-number v-model="securityConfig.lockoutDuration" :min="5" :max="60" /> 分钟
              </el-form-item>
              <el-form-item label="强制双因素认证">
                <el-switch v-model="securityConfig.forceTwoFactor" />
              </el-form-item>
              <el-form-item label="IP白名单">
                <el-switch v-model="securityConfig.ipWhitelistEnabled" />
              </el-form-item>
            </div>

            <el-form-item>
              <el-button type="primary" @click="handleSaveSecurityConfig" :loading="saveLoading">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 邮件通知 -->
      <el-tab-pane label="邮件通知" name="email">
        <div class="p-6">
          <el-form :model="emailConfig" label-width="150px">
            <el-form-item label="启用邮件通知">
              <el-switch v-model="emailConfig.enabled" />
            </el-form-item>
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailConfig.smtpHost" placeholder="smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="emailConfig.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailConfig.fromEmail" placeholder="noreply@bankshield.com" />
            </el-form-item>
            <el-form-item label="发件人名称">
              <el-input v-model="emailConfig.fromName" placeholder="BankShield" />
            </el-form-item>
            <el-form-item label="SMTP用户名">
              <el-input v-model="emailConfig.username" />
            </el-form-item>
            <el-form-item label="SMTP密码">
              <el-input v-model="emailConfig.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="使用SSL">
              <el-switch v-model="emailConfig.useSSL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleTestEmail">测试连接</el-button>
              <el-button type="primary" @click="handleSaveEmailConfig" :loading="saveLoading">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 备份恢复 -->
      <el-tab-pane label="备份恢复" name="backup">
        <div class="p-6">
          <div class="space-y-6">
            <div>
              <h3 class="text-lg font-medium text-gray-900 mb-4">自动备份设置</h3>
              <el-form :model="backupConfig" label-width="150px">
                <el-form-item label="启用自动备份">
                  <el-switch v-model="backupConfig.autoBackup" />
                </el-form-item>
                <el-form-item label="备份频率">
                  <el-select v-model="backupConfig.frequency">
                    <el-option label="每天" value="daily" />
                    <el-option label="每周" value="weekly" />
                    <el-option label="每月" value="monthly" />
                  </el-select>
                </el-form-item>
                <el-form-item label="备份时间">
                  <el-time-picker v-model="backupConfig.backupTime" format="HH:mm" />
                </el-form-item>
                <el-form-item label="保留备份数量">
                  <el-input-number v-model="backupConfig.retentionCount" :min="1" :max="30" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSaveBackupConfig" :loading="saveLoading">保存配置</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div class="pt-6 border-t border-gray-200">
              <h3 class="text-lg font-medium text-gray-900 mb-4">手动备份</h3>
              <el-button type="primary" @click="handleManualBackup" :loading="backupLoading">
                <el-icon class="mr-2"><Download /></el-icon>
                立即备份
              </el-button>
            </div>

            <div class="pt-6 border-t border-gray-200">
              <h3 class="text-lg font-medium text-gray-900 mb-4">备份历史</h3>
              <div class="overflow-hidden">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">备份时间</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">文件大小</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">备份类型</th>
                      <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="backup in backupHistory" :key="backup.id" class="hover:bg-gray-50">
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ backup.time }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ backup.size }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ backup.type }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button @click="handleDownloadBackup(backup)" class="text-blue-600 hover:text-blue-900 mr-3">下载</button>
                        <button @click="handleRestoreBackup(backup)" class="text-green-600 hover:text-green-900 mr-3">恢复</button>
                        <button @click="handleDeleteBackup(backup)" class="text-red-600 hover:text-red-900">删除</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const activeTab = ref('system')
const saveLoading = ref(false)
const backupLoading = ref(false)

const systemConfig = reactive({
  systemName: 'BankShield',
  version: 'v1.0.0',
  sessionTimeout: 30,
  maxFileSize: 10,
  logLevel: 'info',
  logRetentionDays: 90
})

const securityConfig = reactive({
  passwordMinLength: 8,
  passwordComplexity: ['uppercase', 'lowercase', 'number'],
  passwordExpireDays: 90,
  passwordHistory: 5,
  maxLoginAttempts: 5,
  lockoutDuration: 30,
  forceTwoFactor: false,
  ipWhitelistEnabled: false
})

const emailConfig = reactive({
  enabled: true,
  smtpHost: 'smtp.example.com',
  smtpPort: 465,
  fromEmail: 'noreply@bankshield.com',
  fromName: 'BankShield',
  username: '',
  password: '',
  useSSL: true
})

const backupConfig = reactive({
  autoBackup: true,
  frequency: 'daily',
  backupTime: new Date(),
  retentionCount: 7
})

const backupHistory = ref([
  { id: 1, time: '2024-12-30 02:00:00', size: '256 MB', type: '自动备份' },
  { id: 2, time: '2024-12-29 02:00:00', size: '254 MB', type: '自动备份' },
  { id: 3, time: '2024-12-28 14:30:00', size: '250 MB', type: '手动备份' }
])

const handleSaveSystemConfig = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  saveLoading.value = false
  ElMessage.success('系统参数保存成功')
}

const handleResetSystemConfig = () => {
  ElMessage.info('配置已重置')
}

const handleSaveSecurityConfig = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  saveLoading.value = false
  ElMessage.success('安全策略保存成功')
}

const handleTestEmail = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1500))
  saveLoading.value = false
  ElMessage.success('邮件服务器连接成功')
}

const handleSaveEmailConfig = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  saveLoading.value = false
  ElMessage.success('邮件配置保存成功')
}

const handleSaveBackupConfig = async () => {
  saveLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  saveLoading.value = false
  ElMessage.success('备份配置保存成功')
}

const handleManualBackup = async () => {
  backupLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 2000))
  backupLoading.value = false
  ElMessage.success('数据备份完成')
}

const handleDownloadBackup = (backup: any) => {
  ElMessage.success(`开始下载备份: ${backup.time}`)
}

const handleRestoreBackup = async (backup: any) => {
  try {
    await ElMessageBox.confirm(`确定要恢复到 ${backup.time} 的备份吗？此操作将覆盖当前数据！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('数据恢复成功')
  } catch {}
}

const handleDeleteBackup = async (backup: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除 ${backup.time} 的备份吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const index = backupHistory.value.findIndex(b => b.id === backup.id)
    if (index > -1) {
      backupHistory.value.splice(index, 1)
      ElMessage.success('备份已删除')
    }
  } catch {}
}
</script>
