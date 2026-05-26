<template>
  <div class="image-upload-field">
    <div
      class="preview-box"
      role="button"
      tabindex="0"
      @click="triggerSelect"
      @keydown.enter.prevent="triggerSelect"
      @keydown.space.prevent="triggerSelect"
    >
      <img v-if="modelValue" :src="modelValue" alt="preview" class="preview-image">
      <div v-else class="preview-empty">点击上传<br>暂无图片</div>
      <button
        v-if="modelValue"
        class="preview-clear"
        type="button"
        @click.stop="clearValue"
      >
        清空
      </button>
    </div>
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="hidden-input"
      @change="handleChange"
    >
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadApi } from '@/api/upload'

const props = withDefaults(defineProps<{
  modelValue?: string
  keyPrefix?: string
  maxSizeMb?: number
}>(), {
  modelValue: '',
  keyPrefix: 'uploads',
  maxSizeMb: 10,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  uploaded: [value: string]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

function triggerSelect() {
  fileInput.value?.click()
}

function clearValue() {
  emit('update:modelValue', '')
}

async function handleChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片文件')
    input.value = ''
    return
  }

  if (file.size > props.maxSizeMb * 1024 * 1024) {
    ElMessage.warning(`图片大小不能超过 ${props.maxSizeMb}MB`)
    input.value = ''
    return
  }

  uploading.value = true
  try {
    const res = await uploadApi.upload(file, props.keyPrefix) as any
    if (res.code === 200) {
      emit('update:modelValue', res.data.ossUrl)
      emit('uploaded', res.data.ossUrl)
      ElMessage.success('图片上传成功')
    }
  } finally {
    uploading.value = false
    input.value = ''
  }
}
</script>

<style scoped>
.image-upload-field {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-box {
  position: relative;
  width: 88px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--muted));
  border: 1px dashed hsl(var(--border));
  border-radius: 16px;
  overflow: hidden;
  flex: 0 0 auto;
  padding: 0;
  cursor: pointer;
  appearance: none;
}

.preview-box::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 35%, hsl(220 20% 10% / 0.72));
  opacity: 0;
  transition: opacity 0.2s ease;
}

.preview-box:hover::after {
  opacity: 1;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-empty {
  padding: 12px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  text-align: center;
  line-height: 1.5;
}

.preview-clear {
  position: absolute;
  left: 50%;
  bottom: 8px;
  z-index: 1;
  transform: translateX(-50%);
  min-width: 48px;
  padding: 4px 10px;
  border: none;
  border-radius: 999px;
  background: hsl(0 0% 100% / 0.92);
  color: hsl(220 18% 16%);
  font-size: 12px;
  line-height: 1.4;
  opacity: 0;
  cursor: pointer;
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.preview-box:hover .preview-clear {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.preview-clear:hover {
  background: hsl(0 0% 100%);
}

.hidden-input {
  display: none;
}
</style>
