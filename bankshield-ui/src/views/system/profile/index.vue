<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">个人资料</h1>
      <p class="mt-1 text-sm text-gray-500">管理您的个人信息和账户设置</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧头像和基本信息 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex flex-col items-center">
            <div class="relative">
              <div class="h-32 w-32 rounded-full bg-blue-100 flex items-center justify-center text-4xl font-bold text-blue-600">
                {{ userInfo.username.charAt(0).toUpperCase() }}
              </div>
              <button @click="handleUploadAvatar" class="absolute bottom-0 right-0 h-10 w-10 rounded-full bg-blue-600 text-white flex items-center justify-center hover:bg-blue-700">
                <el-icon><Camera /></el-icon>
              </button>
            </div>
            <h3 class="mt-4 text-xl font-bold text-gray-900">{{ userInfo.realName }}</h3>
            <p class="text-sm text-gray-500">@{{ userInfo.username }}</p>
            <div class="mt-4 flex gap-2">
              <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {{ userInfo.role }}
              </span>
              <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                在线
              </span>
            </div>
          </div>

          <div class="mt-6 pt-6 border-t border-gray-200">
            <div class="space-y-3">
              <div class="flex items-center text-sm">
                <el-icon class="text-gray-400 mr-2"><Message /></el-icon>
                <span class="text-gray-500">邮箱：</span>
                <span class="ml-2 text-gray-900">{{ userInfo.email }}</span>
              </div>
              <div class="flex items-center text-sm">
                <el-icon class="text-gray-400 mr-2"><Phone /></el-icon>
                <span class="text-gray-500">手机：</span>
                <span class="ml-2 text-gray-900">{{ userInfo.phone }}</span>
              </div>
              <div class="flex items-center text-sm">
                <el-icon class="text-gray-400 mr-2"><Calendar /></el-icon>
                <span class="text-gray-500">注册时间：</span>
                <span class="ml-2 text-gray-900">{{ userInfo.createTime }}</span>
              </div>
              <div class="flex items-center text-sm">
                <el-icon class="text-gray-400 mr-2"><Clock /></el-icon>
                <span class="text-gray-500">最后登录：</span>
                <span class="ml-2 text-gray-900">{{ userInfo.lastLoginTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧详细信息 -->
      <div class="lg:col-span-2">
        <el-tabs v-model="activeTab" class="bg-white rounded-lg shadow">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="p-6">
              <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" disabled />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="userForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="部门">
                  <el-select v-model="userForm.department" placeholder="请选择部门" class="w-full">
                    <el-option label="技术部" value="tech" />
                    <el-option label="安全部" value="security" />
                    <el-option label="运维部" value="ops" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateBasicInfo" :loading="saveLoading">保存修改</el-button>
                  <el-button @click="handleResetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 安全设置 -->
          <el-tab-pane label="安全设置" name="security">
            <div class="p-6">
              <div class="space-y-6">
                <!-- 修改密码 -->
                <div class="border-b border-gray-200 pb-6">
                  <h3 class="text-lg font-medium text-gray-900 mb-4">修改密码</h3>
                  <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                    <el-form-item label="当前密码" prop="oldPassword">
                      <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                    </el-form-item>
                    <el-form-item label="确认密码" prop="confirmPassword">
                      <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
                    </el-form-item>
                  </el-form>
                </div>

                <!-- 双因素认证 -->
                <div class="border-b border-gray-200 pb-6">
                  <div class="flex items-center justify-between mb-4">
                    <div>
                      <h3 class="text-lg font-medium text-gray-900">双因素认证</h3>
                      <p class="text-sm text-gray-500">增强账户安全性，启用后登录需要验证码</p>
                    </div>
                    <el-switch v-model="securitySettings.twoFactorAuth" @change="handleToggleTwoFactor" />
                  </div>
                </div>

                <!-- 登录设备管理 -->
                <div>
                  <h3 class="text-lg font-medium text-gray-900 mb-4">登录设备</h3>
                  <div class="space-y-3">
                    <div v-for="device in loginDevices" :key="device.id" class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                      <div class="flex items-center">
                        <el-icon class="text-2xl text-gray-400 mr-3"><Monitor /></el-icon>
                        <div>
                          <p class="text-sm font-medium text-gray-900">{{ device.device }}</p>
                          <p class="text-xs text-gray-500">{{ device.location }} · {{ device.time }}</p>
                        </div>
                      </div>
                      <button v-if="!device.current" @click="handleRemoveDevice(device)" class="text-sm text-red-600 hover:text-red-700">
                        移除
                      </button>
                      <span v-else class="text-sm text-green-600">当前设备</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 操作日志 -->
          <el-tab-pane label="操作日志" name="logs">
            <div class="p-6">
              <div class="overflow-hidden">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作时间</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作类型</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作内容</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP地址</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="log in operationLogs" :key="log.id" class="hover:bg-gray-50">
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.time }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ log.type }}</td>
                      <td class="px-6 py-4 text-sm text-gray-500">{{ log.content }}</td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.ip }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Camera, Message, Phone, Calendar, Clock, Monitor } from '@element-plus/icons-vue'

const activeTab = ref('basic')
const saveLoading = ref(false)
const passwordLoading = ref(false)

const userFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const userInfo = reactive({
  username: 'admin',
  realName: '系统管理员',
  email: 'admin@bankshield.com',
  phone: '13800138000',
  role: '管理员',
  department: 'tech',
  createTime: '2024-01-01',
  lastLoginTime: '2024-12-30 13:45:00'
})

const userForm = reactive({
  username: 'admin',
  realName: '系统管理员',
  email: 'admin@bankshield.com',
  phone: '13800138000',
  department: 'tech'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const securitySettings = reactive({
  twoFactorAuth: false
})

const loginDevices = ref([
  { id: 1, device: 'Chrome on macOS', location: '北京市', time: '2024-12-30 13:45', current: true },
  { id: 2, device: 'Safari on iPhone', location: '上海市', time: '2024-12-29 18:20', current: false },
  { id: 3, device: 'Edge on Windows', location: '深圳市', time: '2024-12-28 09:15', current: false }
])

const operationLogs = ref([
  { id: 1, time: '2024-12-30 13:45:00', type: '登录', content: '用户登录系统', ip: '192.168.1.100' },
  { id: 2, time: '2024-12-30 13:40:00', type: '修改', content: '修改用户权限', ip: '192.168.1.100' },
  { id: 3, time: '2024-12-30 13:35:00', type: '查询', content: '查看审计日志', ip: '192.168.1.100' },
  { id: 4, time: '2024-12-30 13:30:00', type: '新增', content: '新增角色配置', ip: '192.168.1.100' }
])

const userRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleUploadAvatar = () => {
  ElMessage.info('头像上传功能开发中')
}

const handleUpdateBasicInfo = async () => {
  if (!userFormRef.value) return
  try {
    await userFormRef.value.validate()
    saveLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    Object.assign(userInfo, userForm)
    ElMessage.success('基本信息更新成功')
  } catch (error) {
    ElMessage.error('请检查表单填写')
  } finally {
    saveLoading.value = false
  }
}

const handleResetForm = () => {
  Object.assign(userForm, userInfo)
  ElMessage.info('表单已重置')
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    passwordFormRef.value.resetFields()
    ElMessage.success('密码修改成功，请重新登录')
  } catch (error) {
    ElMessage.error('请检查表单填写')
  } finally {
    passwordLoading.value = false
  }
}

const handleToggleTwoFactor = (value: boolean) => {
  if (value) {
    ElMessage.success('双因素认证已启用')
  } else {
    ElMessage.warning('双因素认证已关闭')
  }
}

const handleRemoveDevice = async (device: any) => {
  try {
    await ElMessageBox.confirm(`确定要移除设备 "${device.device}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const index = loginDevices.value.findIndex(d => d.id === device.id)
    if (index > -1) {
      loginDevices.value.splice(index, 1)
      ElMessage.success('设备已移除')
    }
  } catch {}
}
</script>
