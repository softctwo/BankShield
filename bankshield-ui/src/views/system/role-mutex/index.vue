<template>
  <div class="role-mutex-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>三权分立管理</span>
        <el-button
          style="float: right; margin-left: 10px"
          type="primary"
          size="small"
          @click="handleTriggerCheck"
        >
          <i class="el-icon-refresh"></i>
          手动检查
        </el-button>
        <el-button
          style="float: right"
          type="info"
          size="small"
          @click="getSeparationStatus"
        >
          <i class="el-icon-info"></i>
          状态查询
        </el-button>
      </div>

      <!-- 统计信息 -->
      <el-row :gutter="20" class="statistics-row">
        <el-col :span="6">
          <div class="statistic-card">
            <div class="statistic-title">系统管理员</div>
            <div class="statistic-value">{{ statistics.systemAdminCount }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="statistic-card">
            <div class="statistic-title">安全策略员</div>
            <div class="statistic-value">{{ statistics.securityAdminCount }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="statistic-card">
            <div class="statistic-title">合规审计员</div>
            <div class="statistic-value">{{ statistics.auditAdminCount }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="statistic-card">
            <div class="statistic-title">违规记录</div>
            <div class="statistic-value" :class="{ 'text-danger': statistics.violationCount > 0 }">
              {{ statistics.violationCount }}
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 违规记录列表 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="违规记录" name="violations">
          <el-table
            v-loading="loading"
            :data="violationList"
            border
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="roleCode" label="违规角色" width="120">
              <template slot-scope="scope">
                <el-tag :type="getRoleType(scope.row.roleCode)">
                  {{ getRoleName(scope.row.roleCode) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="mutexRoleCode" label="互斥角色" width="120">
              <template slot-scope="scope">
                <el-tag :type="getRoleType(scope.row.mutexRoleCode)">
                  {{ getRoleName(scope.row.mutexRoleCode) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="violationType" label="违规类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.violationType === 1 ? 'warning' : 'danger'">
                  {{ getViolationTypeName(scope.row.violationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="violationTime" label="违规时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.violationTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="status" label="处理状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusName(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  type="text"
                  size="small"
                  @click="handleViolation(scope.row)"
                >
                  处理
                </el-button>
                <el-button
                  type="text"
                  size="small"
                  @click="viewViolationDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            :current-page="pageNum"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </el-tab-pane>

        <el-tab-pane label="互斥规则" name="rules">
          <el-table
            v-loading="rulesLoading"
            :data="mutexRules"
            border
            style="width: 100%"
          >
            <el-table-column prop="roleCode" label="角色" width="150">
              <template slot-scope="scope">
                <el-tag :type="getRoleType(scope.row.roleCode)">
                  {{ getRoleName(scope.row.roleCode) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="mutexRoleCode" label="互斥角色" width="150">
              <template slot-scope="scope">
                <el-tag :type="getRoleType(scope.row.mutexRoleCode)">
                  {{ getRoleName(scope.row.mutexRoleCode) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="规则描述" />
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 处理违规对话框 -->
    <el-dialog
      title="处理违规记录"
      :visible.sync="violationDialogVisible"
      width="500px"
    >
      <el-form ref="violationForm" :model="violationForm" label-width="80px">
        <el-form-item label="处理状态">
          <el-radio-group v-model="violationForm.status">
            <el-radio :label="1">已处理</el-radio>
            <el-radio :label="2">已忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="violationForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="violationDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitViolationHandle">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 违规详情对话框 -->
    <el-dialog
      title="违规详情"
      :visible.sync="detailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentViolation.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentViolation.username }}</el-descriptions-item>
        <el-descriptions-item label="违规角色">
          <el-tag :type="getRoleType(currentViolation.roleCode)">
            {{ getRoleName(currentViolation.roleCode) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="互斥角色">
          <el-tag :type="getRoleType(currentViolation.mutexRoleCode)">
            {{ getRoleName(currentViolation.mutexRoleCode) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="违规类型">
          {{ getViolationTypeName(currentViolation.violationType) }}
        </el-descriptions-item>
        <el-descriptions-item label="违规时间">
          {{ formatDateTime(currentViolation.violationTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentViolation.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusType(currentViolation.status)">
            {{ getStatusName(currentViolation.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="currentViolation.handleTime">
          {{ formatDateTime(currentViolation.handleTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" v-if="currentViolation.handleRemark" :span="2">
          {{ currentViolation.handleRemark }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRoleViolations, handleRoleViolation, getMutexRules, getSeparationStatus, triggerRoleCheck } from '@/api/role-mutex'
import { parseTime } from '@/utils'

export default {
  name: 'RoleMutex',
  data() {
    return {
      activeTab: 'violations',
      loading: false,
      rulesLoading: false,
      violationList: [],
      mutexRules: [],
      statistics: {
        systemAdminCount: 0,
        securityAdminCount: 0,
        auditAdminCount: 0,
        violationCount: 0
      },
      pageNum: 1,
      pageSize: 10,
      total: 0,
      violationDialogVisible: false,
      detailDialogVisible: false,
      currentViolation: {},
      violationForm: {
        id: null,
        status: 1,
        handleRemark: '',
        handlerName: ''
      },
      roleMap: {
        'SYSTEM_ADMIN': { name: '系统管理员', type: 'danger' },
        'SECURITY_ADMIN': { name: '安全策略员', type: 'warning' },
        'AUDIT_ADMIN': { name: '合规审计员', type: 'info' }
      }
    }
  },
  created() {
    this.getViolationList()
    this.getMutexRules()
    this.getSeparationStatus()
  },
  methods: {
    // 获取违规记录列表
    async getViolationList() {
      this.loading = true
      try {
        const response = await getRoleViolations({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        if (response.code === 200) {
          this.violationList = response.data.list
          this.total = response.data.total
        }
      } catch (error) {
        this.$message.error('获取违规记录失败')
      } finally {
        this.loading = false
      }
    },

    // 获取互斥规则
    async getMutexRules() {
      this.rulesLoading = true
      try {
        const response = await getMutexRules()
        if (response.code === 200) {
          this.mutexRules = response.data
        }
      } catch (error) {
        this.$message.error('获取互斥规则失败')
      } finally {
        this.rulesLoading = false
      }
    },

    // 获取三权分立状态
    async getSeparationStatus() {
      try {
        const response = await getSeparationStatus()
        if (response.code === 200) {
          // 解析状态信息
          const statusText = response.data
          const matches = statusText.match(/系统管理员=(\d+)人，安全策略员=(\d+)人，合规审计员=(\d+)人，总违规记录=(\d+)条/)
          if (matches) {
            this.statistics.systemAdminCount = parseInt(matches[1])
            this.statistics.securityAdminCount = parseInt(matches[2])
            this.statistics.auditAdminCount = parseInt(matches[3])
            this.statistics.violationCount = parseInt(matches[4])
          }
        }
      } catch (error) {
        console.error('获取状态失败', error)
      }
    },

    // 手动触发检查
    async handleTriggerCheck() {
      try {
        const response = await triggerRoleCheck()
        if (response.code === 200) {
          this.$message.success('角色检查任务已触发')
          setTimeout(() => {
            this.getViolationList()
            this.getSeparationStatus()
          }, 2000)
        }
      } catch (error) {
        this.$message.error('触发检查失败')
      }
    },

    // 处理违规
    handleViolation(row) {
      this.currentViolation = row
      this.violationForm.id = row.id
      this.violationForm.status = 1
      this.violationForm.handleRemark = ''
      this.violationForm.handlerName = this.$store.getters.name
      this.violationDialogVisible = true
    },

    // 提交违规处理
    async submitViolationHandle() {
      try {
        const response = await handleRoleViolation({
          id: this.violationForm.id,
          status: this.violationForm.status,
          handleRemark: this.violationForm.handleRemark,
          handlerName: this.violationForm.handlerName
        })
        if (response.code === 200) {
          this.$message.success('处理成功')
          this.violationDialogVisible = false
          this.getViolationList()
          this.getSeparationStatus()
        }
      } catch (error) {
        this.$message.error('处理失败')
      }
    },

    // 查看违规详情
    viewViolationDetail(row) {
      this.currentViolation = row
      this.detailDialogVisible = true
    },

    // 标签页切换
    handleTabClick(tab) {
      if (tab.name === 'rules') {
        this.getMutexRules()
      }
    },

    // 分页处理
    handleSizeChange(val) {
      this.pageSize = val
      this.getViolationList()
    },

    handleCurrentChange(val) {
      this.pageNum = val
      this.getViolationList()
    },

    // 工具方法
    formatDateTime(time) {
      return parseTime(new Date(time), '{y}-{m}-{d} {h}:{i}:{s}')
    },

    getRoleName(roleCode) {
      const role = this.roleMap[roleCode]
      return role ? role.name : roleCode
    },

    getRoleType(roleCode) {
      const role = this.roleMap[roleCode]
      return role ? role.type : 'info'
    },

    getViolationTypeName(type) {
      return type === 1 ? '手动分配' : '系统检测'
    },

    getStatusName(status) {
      const statusMap = {
        0: '未处理',
        1: '已处理',
        2: '已忽略'
      }
      return statusMap[status] || '未知'
    },

    getStatusType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'info'
      }
      return typeMap[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.role-mutex-container {
  padding: 20px;
}

.statistics-row {
  margin-bottom: 20px;
}

.statistic-card {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  text-align: center;
  border: 1px solid #ebeef5;
}

.statistic-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.statistic-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.text-danger {
  color: #f56c6c !important;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}
</style>