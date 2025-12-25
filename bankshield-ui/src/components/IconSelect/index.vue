<template>
  <div class="icon-select">
    <el-input
      v-model="selectedIcon"
      :placeholder="placeholder"
      :disabled="disabled"
      readonly
    >
      <template #prepend v-if="selectedIcon">
        <el-icon :size="16">
          <component :is="selectedIcon" />
        </el-icon>
      </template>
      <template #append>
        <el-button @click="openDialog">
          <el-icon><Search /></el-icon>
        </el-button>
      </template>
    </el-input>

    <el-dialog
      v-model="dialogVisible"
      title="选择图标"
      width="600px"
      @close="handleClose"
    >
      <div class="icon-container">
        <div
          v-for="icon in availableIcons"
          :key="icon"
          class="icon-item"
          :class="{ active: selectedIcon === icon }"
          @click="selectIcon(icon)"
        >
          <el-icon :size="24">
            <component :is="icon" />
          </el-icon>
          <span class="icon-name">{{ icon }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'

interface Props {
  modelValue: string | null
  placeholder?: string
  disabled?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: string | null): void
  (e: 'change', value: string | null): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择图标',
  disabled: false
})

const emit = defineEmits<Emits>()

const dialogVisible = ref(false)
const tempSelectedIcon = ref<string | null>(null)

const selectedIcon = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
    emit('change', value)
  }
})

// Element Plus 可用图标
const availableIcons = [
  'House',
  'User',
  'Lock',
  'Key',
  'Setting',
  'Tools',
  'DataAnalysis',
  'Coin',
  'MapLocation',
  'Check',
  'Connection',
  'Share',
  'Document',
  'Folder',
  'FolderOpened',
  'Menu',
  'Grid',
  'Operation',
  'Monitor',
  'Warning',
  'InfoFilled',
  'SuccessFilled',
  'CircleCloseFilled',
  'ArrowLeft',
  'ArrowRight',
  'ArrowUp',
  'ArrowDown',
  'Plus',
  'Minus',
  'Delete',
  'Edit',
  'Search',
  'Refresh',
  'View',
  'Hide',
  'Upload',
  'Download',
  'Calendar',
  'Clock',
  'Location',
  'Phone',
  'Message',
  'ChatLineSquare',
  'Promotion'
]

const openDialog = () => {
  if (props.disabled) return
  tempSelectedIcon.value = selectedIcon.value
  dialogVisible.value = true
}

const selectIcon = (icon: string) => {
  tempSelectedIcon.value = icon
}

const handleClose = () => {
  dialogVisible.value = false
  tempSelectedIcon.value = null
}

const handleConfirm = () => {
  selectedIcon.value = tempSelectedIcon.value
  handleClose()
}
</script>

<style lang="less" scoped>
.icon-select {
  width: 100%;
}

.icon-container {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;

  &:hover {
    border-color: #409eff;
    background-color: #f5f7fa;
  }

  &.active {
    border-color: #409eff;
    background-color: #ecf5ff;
  }

  .icon-name {
    margin-top: 5px;
    font-size: 11px;
    color: #606266;
    word-break: break-all;
    line-height: 1.2;
  }
}
</style>