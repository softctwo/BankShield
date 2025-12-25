<template>
  <el-dialog
    title="生成新密钥"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="120px"
      v-loading="loading"
    >
      <el-form-item label="密钥名称" prop="keyName">
        <el-input
          v-model="form.keyName"
          placeholder="请输入密钥名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="密钥类型" prop="keyType">
        <el-select
          v-model="form.keyType"
          placeholder="请选择密钥类型"
          @change="handleKeyTypeChange"
        >
          <el-option label="国密SM2 (非对称加密)" value="SM2" />
          <el-option label="国密SM3 (哈希算法)" value="SM3" disabled />
          <el-option label="国密SM4 (对称加密)" value="SM4" />
          <el-option label="AES (对称加密)" value="AES" />
          <el-option label="RSA (非对称加密)" value="RSA" />
        </el-select>
      </el-form-item>

      <el-form-item label="密钥用途" prop="keyUsage">
        <el-select v-model="form.keyUsage" placeholder="请选择密钥用途">
          <el-option label="加密" value="ENCRYPT" />
          <el-option label="解密" value="DECRYPT" />
          <el-option label="签名" value="SIGN" />
          <el-option label="验签" value="VERIFY" />
        </el-select>
      </el-form-item>

      <el-form-item label="密钥长度" prop="keyLength">
        <el-select v-model="form.keyLength" placeholder="请选择密钥长度">
          <el-option 
            v-if="form.keyType === 'SM2'" 
            label="256位 (默认)" 
            :value="256" 
          />
          <el-option 
            v-if="form.keyType === 'SM4'" 
            label="128位 (默认)" 
            :value="128" 
          />
          <el-option 
            v-if="form.keyType === 'AES'" 
            label="128位" 
            :value="128" 
          />
          <el-option 
            v-if="form.keyType === 'AES'" 
            label="192位" 
            :value="192" 
          />
          <el-option 
            v-if="form.keyType === 'AES'" 
            label="256位 (默认)" 
            :value="256" 
          />
          <el-option 
            v-if="form.keyType === 'RSA'" 
            label="1024位" 
            :value="1024" 
          />
          <el-option 
            v-if="form.keyType === 'RSA'" 
            label="2048位 (默认)" 
            :value="2048" 
          />
          <el-option 
            v-if="form.keyType === 'RSA'" 
            label="4096位" 
            :value="4096" 
          />
        </el-select>
      </el-form-item>

      <el-form-item label="过期时间" prop="expireDays">
        <el-input-number
          v-model="form.expireDays"
          :min="1"
          :max="3650"
          :step="30"
          controls-position="right"
        />
        <span class="unit-label">天</span>
      </el-form-item>

      <el-form-item label="轮换周期" prop="rotationCycle">
        <el-input-number
          v-model="form.rotationCycle"
          :min="1"
          :max="3650"
          :step="30"
          controls-position="right"
        />
        <span class="unit-label">天</span>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          type="textarea"
          v-model="form.description"
          placeholder="请输入密钥描述信息"
          :rows="3"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 算法说明 -->
      <el-form-item v-if="form.keyType" label="算法说明">
        <el-alert
          :title="getAlgorithmTitle(form.keyType)"
          :description="getAlgorithmDescription(form.keyType)"
          :type="getAlgorithmType(form.keyType)"
          :closable="false"
          show-icon
        />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        生 成
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { generateKey, getSupportedKeyTypes } from '@/api/key'

export default {
  name: 'GenerateKeyDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      form: {
        keyName: '',
        keyType: '',
        keyUsage: '',
        keyLength: null,
        expireDays: 365,
        rotationCycle: 90,
        description: ''
      },
      rules: {
        keyName: [
          { required: true, message: '请输入密钥名称', trigger: 'blur' },
          { min: 2, max: 100, message: '密钥名称长度在 2 到 100 个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_-]+$/, message: '密钥名称只能包含字母、数字、下划线和连字符', trigger: 'blur' }
        ],
        keyType: [
          { required: true, message: '请选择密钥类型', trigger: 'change' }
        ],
        keyUsage: [
          { required: true, message: '请选择密钥用途', trigger: 'change' }
        ],
        expireDays: [
          { required: true, message: '请输入过期时间', trigger: 'blur' },
          { type: 'number', min: 1, max: 3650, message: '过期时间必须在 1 到 3650 天之间', trigger: 'blur' }
        ],
        rotationCycle: [
          { required: true, message: '请输入轮换周期', trigger: 'blur' },
          { type: 'number', min: 1, max: 3650, message: '轮换周期必须在 1 到 3650 天之间', trigger: 'blur' }
        ]
      },
      supportedKeyTypes: []
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.resetForm()
        this.loadSupportedKeyTypes()
      }
    }
  },
  methods: {
    // 加载支持的密钥类型
    async loadSupportedKeyTypes() {
      try {
        const response = await getSupportedKeyTypes()
        if (response.code === 200) {
          this.supportedKeyTypes = response.data
        }
      } catch (error) {
        console.error('加载支持的密钥类型失败:', error)
      }
    },

    // 密钥类型变化
    handleKeyTypeChange() {
      // 重置密钥长度
      this.form.keyLength = null
      
      // 根据密钥类型设置默认密钥长度
      switch (this.form.keyType) {
        case 'SM2':
          this.form.keyLength = 256
          break
        case 'SM4':
          this.form.keyLength = 128
          break
        case 'AES':
          this.form.keyLength = 256
          break
        case 'RSA':
          this.form.keyLength = 2048
          break
      }

      // 根据密钥类型调整用途选项
      if (this.form.keyType === 'SM3') {
        this.form.keyUsage = ''
      }
    },

    // 获取算法标题
    getAlgorithmTitle(keyType) {
      const titles = {
        'SM2': '国密SM2非对称加密算法',
        'SM3': '国密SM3哈希算法',
        'SM4': '国密SM4对称加密算法',
        'AES': 'AES对称加密算法',
        'RSA': 'RSA非对称加密算法'
      }
      return titles[keyType] || '未知算法'
    },

    // 获取算法描述
    getAlgorithmDescription(keyType) {
      const descriptions = {
        'SM2': '基于椭圆曲线密码学的非对称加密算法，用于数字签名和密钥交换，密钥长度为256位。',
        'SM3': '国密哈希算法，用于生成消息摘要，不需要密钥。',
        'SM4': '分组对称加密算法，用于数据加密，密钥长度为128位，支持ECB/CBC/CTR模式。',
        'AES': '高级加密标准，对称加密算法，支持128/192/256位密钥长度。',
        'RSA': '非对称加密算法，支持数据加密和数字签名，常用密钥长度为2048位。'
      }
      return descriptions[keyType] || '暂无描述'
    },

    // 获取算法类型
    getAlgorithmType(keyType) {
      const types = {
        'SM2': 'info',
        'SM3': 'warning',
        'SM4': 'success',
        'AES': 'success',
        'RSA': 'info'
      }
      return types[keyType] || 'info'
    },

    // 提交表单
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) {
          return
        }

        this.loading = true
        try {
          const requestData = {
            ...this.form,
            createdBy: this.userInfo.username
          }

          const response = await generateKey(requestData)
          if (response.code === 200) {
            this.$message.success('密钥生成成功')
            this.$emit('success', response.data)
            this.handleClose()
          } else {
            this.$message.error(response.message || '密钥生成失败')
          }
        } catch (error) {
          this.$message.error('密钥生成失败')
          console.error('密钥生成失败:', error)
        } finally {
          this.loading = false
        }
      })
    },

    // 取消
    handleCancel() {
      this.handleClose()
    },

    // 关闭对话框
    handleClose() {
      this.$emit('update:visible', false)
      this.resetForm()
    },

    // 重置表单
    resetForm() {
      this.$refs.form && this.$refs.form.resetFields()
      this.form = {
        keyName: '',
        keyType: '',
        keyUsage: '',
        keyLength: null,
        expireDays: 365,
        rotationCycle: 90,
        description: ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.unit-label {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}
</style>