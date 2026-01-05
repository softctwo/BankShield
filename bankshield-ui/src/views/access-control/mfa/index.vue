<template>
  <div class="mfa-config-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="config-card">
          <template #header>
            <div class="card-header">
              <span>MFA配置</span>
            </div>
          </template>

          <el-form :model="mfaForm" label-width="120px">
            <el-form-item label="用户ID">
              <el-input v-model="mfaForm.userId" placeholder="请输入用户ID" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="mfaForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="MFA类型">
              <el-select v-model="mfaForm.mfaType" placeholder="请选择MFA类型">
                <el-option label="短信验证码" value="SMS" />
                <el-option label="邮箱验证码" value="EMAIL" />
                <el-option label="TOTP验证器" value="TOTP" />
                <el-option label="生物识别" value="BIOMETRIC" />
              </el-select>
            </el-form-item>

            <el-form-item v-if="mfaForm.mfaType === 'SMS'" label="手机号">
              <el-input v-model="mfaForm.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item v-if="mfaForm.mfaType === 'EMAIL'" label="邮箱">
              <el-input v-model="mfaForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item v-if="mfaForm.mfaType === 'TOTP'" label="TOTP密钥">
              <el-input v-model="mfaForm.totpSecret" placeholder="点击生成密钥" readonly>
                <template #append>
                  <el-button @click="handleGenerateTotpSecret">生成</el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item v-if="mfaForm.mfaType === 'TOTP' && mfaForm.totpSecret" label="二维码">
              <div class="qrcode-container">
                <el-image :src="qrcodeUrl" style="width: 200px; height: 200px" />
                <p class="qrcode-tip">使用Google Authenticator扫描二维码</p>
              </div>
            </el-form-item>

            <el-form-item label="启用状态">
              <el-switch v-model="mfaForm.mfaEnabled" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSaveMfaConfig">保存配置</el-button>
              <el-button @click="handleGenerateBackupCodes">生成备用码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="verify-card">
          <template #header>
            <div class="card-header">
              <span>MFA验证测试</span>
            </div>
          </template>

          <el-form :model="verifyForm" label-width="120px">
            <el-form-item label="用户ID">
              <el-input v-model="verifyForm.userId" placeholder="请输入用户ID" />
            </el-form-item>
            <el-form-item label="MFA类型">
              <el-select v-model="verifyForm.mfaType" placeholder="请选择MFA类型">
                <el-option label="短信验证码" value="SMS" />
                <el-option label="邮箱验证码" value="EMAIL" />
                <el-option label="TOTP验证器" value="TOTP" />
                <el-option label="生物识别" value="BIOMETRIC" />
              </el-select>
            </el-form-item>
            <el-form-item label="验证码">
              <el-input v-model="verifyForm.code" placeholder="请输入验证码" maxlength="6" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleVerifyMfa">验证</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div v-if="verifyResult !== null" class="verify-result">
            <el-result
              :icon="verifyResult ? 'success' : 'error'"
              :title="verifyResult ? '验证成功' : '验证失败'"
              :sub-title="verifyResult ? 'MFA验证通过' : '验证码错误或已过期'"
            />
          </div>
        </el-card>

        <el-card v-if="backupCodes.length > 0" class="backup-codes-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>备用验证码</span>
              <el-tag type="warning">请妥善保管</el-tag>
            </div>
          </template>
          <el-alert
            title="重要提示"
            description="备用验证码仅显示一次，请立即保存到安全的地方。每个备用码只能使用一次。"
            type="warning"
            :closable="false"
            style="margin-bottom: 15px"
          />
          <div class="backup-codes-list">
            <el-tag
              v-for="(code, index) in backupCodes"
              :key="index"
              size="large"
              style="margin: 5px; font-family: monospace"
            >
              {{ code }}
            </el-tag>
          </div>
          <el-button type="primary" @click="handleCopyBackupCodes" style="margin-top: 15px">
            复制所有备用码
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  configureMfa,
  verifyMfa,
  generateTotpSecret,
  generateBackupCodes,
  type MfaConfig
} from '@/api/access-control'

const mfaForm = reactive<MfaConfig>({
  userId: 0,
  username: '',
  mfaType: 'TOTP',
  mfaEnabled: false,
  phone: '',
  email: '',
  totpSecret: ''
})

const verifyForm = reactive({
  userId: 0,
  mfaType: 'TOTP',
  code: ''
})

const verifyResult = ref<boolean | null>(null)
const backupCodes = ref<string[]>([])

const qrcodeUrl = computed(() => {
  if (!mfaForm.totpSecret || !mfaForm.username) {
    return ''
  }
  const otpauthUrl = `otpauth://totp/BankShield:${mfaForm.username}?secret=${mfaForm.totpSecret}&issuer=BankShield`
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(otpauthUrl)}`
})

const handleGenerateTotpSecret = async () => {
  try {
    const res = await generateTotpSecret()
    if (res.code === 200) {
      mfaForm.totpSecret = res.data
      ElMessage.success('TOTP密钥生成成功')
    }
  } catch (error) {
    ElMessage.error('生成TOTP密钥失败')
  }
}

const handleSaveMfaConfig = async () => {
  if (!mfaForm.userId || !mfaForm.username) {
    ElMessage.warning('请输入用户ID和用户名')
    return
  }

  if (mfaForm.mfaType === 'SMS' && !mfaForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }

  if (mfaForm.mfaType === 'EMAIL' && !mfaForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }

  if (mfaForm.mfaType === 'TOTP' && !mfaForm.totpSecret) {
    ElMessage.warning('请先生成TOTP密钥')
    return
  }

  try {
    await configureMfa(mfaForm)
    ElMessage.success('MFA配置保存成功')
  } catch (error) {
    ElMessage.error('保存MFA配置失败')
  }
}

const handleVerifyMfa = async () => {
  if (!verifyForm.userId || !verifyForm.code) {
    ElMessage.warning('请输入用户ID和验证码')
    return
  }

  try {
    const res = await verifyMfa(verifyForm.userId, verifyForm.mfaType, verifyForm.code)
    if (res.code === 200) {
      verifyResult.value = res.data
      if (res.data) {
        ElMessage.success('MFA验证成功')
      } else {
        ElMessage.error('MFA验证失败')
      }
    }
  } catch (error) {
    verifyResult.value = false
    ElMessage.error('MFA验证失败')
  }
}

const handleGenerateBackupCodes = async () => {
  try {
    const res = await generateBackupCodes()
    if (res.code === 200) {
      backupCodes.value = res.data
      ElMessage.success('备用验证码生成成功')
    }
  } catch (error) {
    ElMessage.error('生成备用验证码失败')
  }
}

const handleCopyBackupCodes = () => {
  const text = backupCodes.value.join('\n')
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('备用验证码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style scoped lang="less">
.mfa-config-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .qrcode-container {
    text-align: center;

    .qrcode-tip {
      margin-top: 10px;
      color: #909399;
      font-size: 12px;
    }
  }

  .verify-result {
    margin-top: 20px;
  }

  .backup-codes-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
