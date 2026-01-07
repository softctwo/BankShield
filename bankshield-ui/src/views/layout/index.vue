<template>
  <div class="layout-container">
    <!-- 侧边栏菜单 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo" @click="goToHome">
        <el-icon class="logo-icon"><Lock /></el-icon>
        <span v-if="!isCollapse" class="logo-text">BankShield</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <!-- 首页 -->
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 数据加密管理 -->
        <el-sub-menu index="encrypt">
          <template #title>
            <el-icon><Key /></el-icon>
            <span>数据加密</span>
          </template>
          <el-menu-item index="/encrypt/key">密钥管理</el-menu-item>
          <el-menu-item index="/encrypt/strategy">加密策略</el-menu-item>
          <el-menu-item index="/encrypt/task">加密任务</el-menu-item>
          <el-menu-item index="/encrypt/key/usage">密钥统计</el-menu-item>
        </el-sub-menu>

        <!-- 访问控制 -->
        <el-sub-menu index="access-control">
          <template #title>
            <el-icon><User /></el-icon>
            <span>访问控制</span>
          </template>
          <el-menu-item index="/access-control/policy">权限策略</el-menu-item>
          <el-menu-item index="/access-control/audit">访问审计</el-menu-item>
          <el-menu-item index="/access-control/mfa">MFA配置</el-menu-item>
          <el-menu-item index="/access-control/ip">IP访问控制</el-menu-item>
        </el-sub-menu>

        <!-- 审计追踪 -->
        <el-sub-menu index="audit">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>审计追踪</span>
          </template>
          <el-menu-item index="/audit/operation">操作日志</el-menu-item>
          <el-menu-item index="/audit/security">安全审计</el-menu-item>
          <el-menu-item index="/audit/analysis">审计分析</el-menu-item>
        </el-sub-menu>

        <!-- 数据脱敏 -->
        <el-sub-menu index="desensitization">
          <template #title>
            <el-icon><Hide /></el-icon>
            <span>数据脱敏</span>
          </template>
          <el-menu-item index="/desensitization/rule">脱敏规则</el-menu-item>
          <el-menu-item index="/desensitization/log">脱敏日志</el-menu-item>
        </el-sub-menu>

        <!-- 合规性检查 -->
        <el-sub-menu index="compliance">
          <template #title>
            <el-icon><CircleCheck /></el-icon>
            <span>合规检查</span>
          </template>
          <el-menu-item index="/compliance/rule">合规规则</el-menu-item>
          <el-menu-item index="/compliance/task">检查任务</el-menu-item>
          <el-menu-item index="/compliance/report">合规报告</el-menu-item>
          <el-menu-item index="/compliance/dashboard">合规Dashboard</el-menu-item>
        </el-sub-menu>

        <!-- 安全态势 -->
        <el-sub-menu index="security">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>安全态势</span>
          </template>
          <el-menu-item index="/security/dashboard">安全大屏</el-menu-item>
          <el-menu-item index="/security/threat">威胁管理</el-menu-item>
          <el-menu-item index="/security/baseline">安全基线</el-menu-item>
        </el-sub-menu>

        <!-- 数据血缘 -->
        <el-sub-menu index="lineage">
          <template #title>
            <el-icon><Share /></el-icon>
            <span>数据血缘</span>
          </template>
          <el-menu-item index="/lineage/track">血缘追踪</el-menu-item>
          <el-menu-item index="/lineage/analysis">影响分析</el-menu-item>
          <el-menu-item index="/lineage/enhanced/discovery">血缘发现</el-menu-item>
          <el-menu-item index="/lineage/enhanced/data-map">数据地图</el-menu-item>
        </el-sub-menu>

        <!-- 安全扫描 -->
        <el-sub-menu index="security-scan">
          <template #title>
            <el-icon><Search /></el-icon>
            <span>安全扫描</span>
          </template>
          <el-menu-item index="/security-scan/task">扫描任务</el-menu-item>
          <el-menu-item index="/security-scan/vulnerability">漏洞管理</el-menu-item>
          <el-menu-item index="/security-scan/dashboard">扫描Dashboard</el-menu-item>
        </el-sub-menu>

        <!-- 分类分级 -->
        <el-sub-menu index="classification">
          <template #title>
            <el-icon><Folder /></el-icon>
            <span>分类分级</span>
          </template>
          <el-menu-item index="/classification/asset-management">资产管理</el-menu-item>
          <el-menu-item index="/classification/sensitive">敏感数据</el-menu-item>
        </el-sub-menu>

        <!-- AI智能分析 -->
        <el-sub-menu index="ai">
          <template #title>
            <el-icon><MagicStick /></el-icon>
            <span>AI分析</span>
          </template>
          <el-menu-item index="/ai/analysis">智能分析</el-menu-item>
          <el-menu-item index="/ai/prediction">威胁预测</el-menu-item>
        </el-sub-menu>

        <!-- 区块链存证 -->
        <el-sub-menu index="blockchain">
          <template #title>
            <el-icon><Link /></el-icon>
            <span>区块链存证</span>
          </template>
          <el-menu-item index="/blockchain/dashboard">存证概览</el-menu-item>
          <el-menu-item index="/blockchain/evidence">存证管理</el-menu-item>
          <el-menu-item index="/blockchain/verify">存证验证</el-menu-item>
          <el-menu-item index="/blockchain/records">存证记录</el-menu-item>
        </el-sub-menu>

        <!-- 监控中心 -->
        <el-sub-menu index="monitor">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>监控中心</span>
          </template>
          <el-menu-item index="/monitor/dashboard">监控大屏</el-menu-item>
          <el-menu-item index="/monitor/alert">告警管理</el-menu-item>
        </el-sub-menu>

        <!-- 系统管理 -->
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
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
    </el-container>
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
  SwitchButton,
  Document,
  Hide,
  CircleCheck,
  Warning,
  Share,
  Search,
  Folder,
  MagicStick,
  Link,
  Fold,
  Expand
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前页面标题
const currentTitle = computed(() => {
  return route.meta?.title as string || ''
})

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
  flex-direction: row;
  background-color: #f5f5f5;

  .layout-aside {
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      background-color: #263445;

      .logo-icon {
        font-size: 24px;
        color: #409eff;
      }

      .logo-text {
        font-size: 18px;
        font-weight: bold;
        color: #fff;
        margin-left: 10px;
      }
    }

    .sidebar-menu {
      border: none;
      height: calc(100vh - 60px);
      overflow-y: auto;

      &:not(.el-menu--collapse) {
        width: 220px;
      }
    }
  }

  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

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
        gap: 20px;

        .collapse-btn {
          font-size: 20px;
          cursor: pointer;
          color: #606266;

          &:hover {
            color: #409eff;
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
}

// 响应式设计
@media (max-width: 768px) {
  .layout-container {
    .layout-aside {
      position: fixed;
      z-index: 1000;
      height: 100vh;
    }

    .main-container {
      margin-left: 64px;

      .layout-header .header-right {
        gap: 10px;

        .user-info .username {
          display: none;
        }
      }

      .layout-main {
        padding: 10px;
      }
    }
  }
}
</style>
