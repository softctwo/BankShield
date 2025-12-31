<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 左侧背景区域 -->
      <div class="login-bg">
        <div class="bg-content">
          <h1 class="system-title">BankShield</h1>
          <p class="system-desc">银行数据安全管理系统</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><Lock /></el-icon>
              <span>数据加密保护</span>
            </div>
            <div class="feature-item">
              <el-icon><View /></el-icon>
              <span>实时安全监控</span>
            </div>
            <div class="feature-item">
              <el-icon><Document /></el-icon>
              <span>完整审计追踪</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form">
        <div class="form-header">
          <h2>系统登录</h2>
          <p>请输入您的账号密码</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form-content"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <el-button type="text" @click="forgotPassword">忘记密码？</el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '立即登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 快速登录选项 -->
        <div class="quick-login">
          <div class="divider">
            <span>快速登录</span>
          </div>
          <div class="quick-options">
            <el-button
              v-for="user in quickUsers"
              :key="user.username"
              size="small"
              @click="quickLogin(user)"
            >
              {{ user.name }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, View, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 记住密码
const rememberMe = ref(false)

// 加载状态
const loading = ref(false)

// 快速登录用户
const quickUsers = [
  { username: 'admin', password: 'admin123', name: '管理员' },
  { username: 'audit', password: 'audit123', name: '审计员' },
  { username: 'operator', password: 'op123456', name: '操作员' }
]

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 模拟登录API调用
    await simulateLogin(loginForm.username, loginForm.password)
    
    // 保存用户信息到store
    await userStore.login({
      username: loginForm.username,
      token: 'mock-token-' + Date.now(),
      roles: getUserRoles(loginForm.username)
    })
    
    ElMessage.success('登录成功')
    
    // 跳转到首页
    router.push('/dashboard')
    
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 模拟登录API
const simulateLogin = (username: string, password: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 简单的用户验证
      const validUsers = {
        'admin': 'admin123',
        'audit': 'audit123',
        'operator': 'op123456'
      }
      
      if (validUsers[username as keyof typeof validUsers] === password) {
        resolve()
      } else {
        reject(new Error('用户名或密码错误'))
      }
    }, 1000)
  })
}

// 获取用户角色
const getUserRoles = (username: string): string[] => {
  const roleMap: Record<string, string[]> = {
    'admin': ['admin', 'audit-admin', 'operator'],
    'audit': ['audit-admin', 'operator'],
    'operator': ['operator']
  }
  return roleMap[username] || ['operator']
}

// 快速登录
const quickLogin = (user: any) => {
  loginForm.username = user.username
  loginForm.password = user.password
  handleLogin()
}

// 忘记密码
const forgotPassword = () => {
  ElMessage.info('请联系系统管理员重置密码')
}
</script>

<style scoped lang="less">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;

  .login-box {
    width: 900px;
    height: 600px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    display: flex;
    overflow: hidden;

    .login-bg {
      flex: 1;
      background: linear-gradient(135deg, #5470c6 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: -50%;
        right: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
        animation: float 6s ease-in-out infinite;
      }

      @keyframes float {
        0%, 100% { transform: translateY(0px); }
        50% { transform: translateY(-20px); }
      }

      .bg-content {
        text-align: center;
        z-index: 1;

        .system-title {
          font-size: 36px;
          font-weight: bold;
          margin-bottom: 10px;
          text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }

        .system-desc {
          font-size: 16px;
          margin-bottom: 40px;
          opacity: 0.9;
        }

        .features {
          .feature-item {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
            font-size: 14px;

            .el-icon {
              margin-right: 8px;
              font-size: 18px;
            }
          }
        }
      }
    }

    .login-form {
      flex: 1;
      padding: 60px 50px;
      display: flex;
      flex-direction: column;

      .form-header {
        text-align: center;
        margin-bottom: 40px;

        h2 {
          font-size: 28px;
          color: #303133;
          margin-bottom: 10px;
        }

        p {
          color: #909399;
          font-size: 14px;
        }
      }

      .login-form-content {
        flex: 1;

        .el-form-item {
          margin-bottom: 24px;

          .el-input {
            height: 48px;

            :deep(.el-input__inner) {
              height: 48px;
              line-height: 48px;
            }
          }
        }

        .form-options {
          display: flex;
          justify-content: space-between;
          align-items: center;
          width: 100%;
          font-size: 14px;

          .el-button--text {
            color: #409eff;
            padding: 0;
            
            &:hover {
              color: #66b1ff;
            }
          }
        }

        .login-btn {
          width: 100%;
          height: 48px;
          font-size: 16px;
          font-weight: 500;
          border-radius: 8px;
          background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
          border: none;

          &:hover {
            background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
          }
        }
      }

      .quick-login {
        margin-top: 30px;

        .divider {
          text-align: center;
          margin-bottom: 20px;
          position: relative;

          &::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 0;
            right: 0;
            height: 1px;
            background: #e4e7ed;
          }

          span {
            background: white;
            padding: 0 15px;
            color: #909399;
            font-size: 12px;
            position: relative;
            z-index: 1;
          }
        }

        .quick-options {
          display: flex;
          justify-content: center;
          gap: 10px;

          .el-button {
            border-radius: 20px;
            border: 1px solid #e4e7ed;
            color: #606266;
            
            &:hover {
              border-color: #409eff;
              color: #409eff;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-container {
    .login-box {
      width: 95%;
      height: auto;
      flex-direction: column;
      margin: 20px;

      .login-bg {
        height: 200px;
        padding: 20px;

        .bg-content {
          .system-title {
            font-size: 24px;
          }

          .system-desc {
            font-size: 14px;
            margin-bottom: 20px;
          }

          .features {
            display: flex;
            justify-content: space-around;

            .feature-item {
              flex-direction: column;
              font-size: 12px;
              margin-bottom: 0;

              .el-icon {
                margin-right: 0;
                margin-bottom: 4px;
              }
            }
          }
        }
      }

      .login-form {
        padding: 40px 30px;

        .form-header {
          margin-bottom: 30px;

          h2 {
            font-size: 24px;
          }
        }
      }
    }
  }
}
</style>
