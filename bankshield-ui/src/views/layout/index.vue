<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <div class="logo" @click="goToHome" style="cursor: pointer;">
          <el-icon class="logo-icon"><Lock /></el-icon>
          <span class="logo-text">BankShield</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :router="true"
          class="header-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>数据大屏</span>
          </el-menu-item>
          <el-sub-menu index="security">
            <template #title>
              <el-icon><Key /></el-icon>
              <span>安全管理</span>
            </template>
            <el-menu-item index="/classification/asset-management">资产分类</el-menu-item>
            <el-menu-item index="/encrypt/key">密钥管理</el-menu-item>
            <el-menu-item index="/audit/operation">审计日志</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="monitor">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>监控中心</span>
            </template>
            <el-menu-item index="/monitor/dashboard">安全监控</el-menu-item>
            <el-menu-item index="/ai/analysis">AI分析</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/dept">部门管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      
      <div class="header-right">
        <!-- 通知中心 -->
        <el-badge :value="notificationCount" class="notification-badge">
          <el-button :icon="Bell" circle @click="showNotifications" />
        </el-badge>
        
        <!-- 用户信息 -->
        <el-dropdown @command="handleUserCommand">
          <div class="user-info">
            <el-avatar :size="32" :src="userStore.userInfo?.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="layout-main">
      <router-view />
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lock,
  DataBoard,
  Key,
  Monitor,
  Setting,
  Bell,
  User,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 通知数量
const notificationCount = ref(3)

// 显示通知
const showNotifications = () => {
  ElMessage.info('暂无新通知')
}

// 返回首页
const goToHome = () => {
  router.push('/dashboard')
}

// 处理用户下拉菜单命令
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style scoped lang="less">
.layout-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;

  .layout-header {
    background: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 60px;

    .header-left {
      display: flex;
      align-items: center;
      flex: 1;

      .logo {
        display: flex;
        align-items: center;
        margin-right: 40px;
        cursor: pointer;

        .logo-icon {
          font-size: 24px;
          color: #409eff;
          margin-right: 8px;
        }

        .logo-text {
          font-size: 18px;
          font-weight: bold;
          color: #303133;
        }
      }

      .header-menu {
        border: none;
        background: transparent;

        :deep(.el-menu-item) {
          border: none;
          height: 60px;
          line-height: 60px;

          &:hover {
            background-color: #f5f7fa;
          }

          &.is-active {
            background-color: #ecf5ff;
            color: #409eff;
            border-bottom: 2px solid #409eff;
          }
        }

        :deep(.el-sub-menu) {
          .el-sub-menu__title {
            border: none;
            height: 60px;
            line-height: 60px;

            &:hover {
              background-color: #f5f7fa;
            }
          }
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .notification-badge {
        :deep(.el-badge__content) {
          transform: translateY(-10px) translateX(10px);
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 8px 12px;
        border-radius: 8px;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        .username {
          margin: 0 8px;
          font-size: 14px;
          color: #303133;
        }

        .dropdown-icon {
          font-size: 12px;
          color: #909399;
          transition: transform 0.3s;
        }

        &:hover .dropdown-icon {
          transform: rotate(180deg);
        }
      }
    }
  }

  .layout-main {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    background-color: #f5f5f5;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .layout-container {
    .layout-header {
      padding: 0 10px;

      .header-left {
        .logo {
          margin-right: 20px;

          .logo-text {
            display: none;
          }
        }

        .header-menu {
          :deep(.el-menu-item),
          :deep(.el-sub-menu .el-sub-menu__title) {
            padding: 0 10px;
            font-size: 12px;

            span {
              display: none;
            }
          }
        }
      }

      .header-right {
        gap: 10px;

        .user-info {
          .username {
            display: none;
          }
        }
      }
    }

    .layout-main {
      padding: 10px;
    }
  }
}
</style>
