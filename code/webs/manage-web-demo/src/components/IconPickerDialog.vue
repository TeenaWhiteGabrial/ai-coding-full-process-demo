<template>
  <el-dialog
    v-model="visibleProxy"
    title="选择图标"
    width="760px"
    destroy-on-close
    :close-on-click-modal="false"
    class="manage-editor-dialog"
  >
    <template #header>
      <div class="dialog-hero">
        <div class="dialog-title">菜单图标面板</div>
        <div class="dialog-subtitle">从预设图标库中选择一个最适合当前菜单的图标。</div>
      </div>
    </template>

    <div class="dialog-section">
      <div class="section-title">图标列表</div>
      <div class="icon-grid">
        <button
          v-for="icon in icons"
          :key="icon"
          type="button"
          class="icon-card"
          :class="{ active: icon === modelValue }"
          @click="selectIcon(icon)"
        >
          <el-icon class="icon-card-preview"><component :is="icon" /></el-icon>
          <span class="icon-card-name">{{ icon }}</span>
        </button>
      </div>
    </div>

    <template #footer>
      <el-button @click="visibleProxy = false">关闭</el-button>
      <el-button v-if="modelValue" text @click="selectIcon('')">清空选择</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { MENU_ICON_OPTIONS } from '@/constants/menu-icons'

const props = defineProps<{
  modelValue?: string
  visible: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:visible': [value: boolean]
}>()

const icons = MENU_ICON_OPTIONS

const visibleProxy = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

function selectIcon(icon: string) {
  emit('update:modelValue', icon)
  if (icon) {
    visibleProxy.value = false
  }
}
</script>

<style scoped>
.dialog-hero {
  padding: 4px 0 2px;
}

.dialog-title {
  color: hsl(var(--foreground));
  font-size: 24px;
  font-weight: 700;
}

.dialog-subtitle {
  margin-top: 8px;
  color: hsl(var(--muted-foreground));
  line-height: 1.7;
}

.dialog-section {
  padding: 18px;
  background: hsl(var(--muted) / 0.55);
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
}

.section-title {
  margin-bottom: 14px;
  color: hsl(var(--foreground));
  font-size: 15px;
  font-weight: 700;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
  gap: 12px;
}

.icon-card {
  display: grid;
  gap: 10px;
  align-items: center;
  justify-items: center;
  padding: 16px 10px;
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-card:hover,
.icon-card.active {
  border-color: hsl(var(--primary));
  background: hsl(var(--secondary));
  box-shadow: var(--ai-glow-sm);
}

.icon-card-preview {
  font-size: 22px;
  color: hsl(var(--foreground));
}

.icon-card-name {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  text-align: center;
  word-break: break-word;
}

:deep(.manage-editor-dialog .el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.manage-editor-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 22px 24px 8px;
  border-bottom: 1px solid hsl(var(--border));
}

:deep(.manage-editor-dialog .el-dialog__body) {
  padding: 20px 24px 12px;
}

:deep(.manage-editor-dialog .el-dialog__footer) {
  padding: 0 24px 20px;
}
</style>
