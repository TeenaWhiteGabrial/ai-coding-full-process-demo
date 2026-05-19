<template>
  <div class="image-upload-field">
    <div class="preview-box">
      <img v-if="modelValue" :src="modelValue" alt="preview" class="preview-image">
      <div v-else class="preview-empty">暂无图片</div>
    </div>
    <div class="actions-box">
      <el-button type="primary" plain :loading="uploading" @click="triggerSelect">上传图片</el-button>
      <el-button v-if="modelValue" text @click="clearValue">清空</el-button>
      <div class="tip-text">{{ tip }}</div>
      <div class="tip-text">大小不超过 {{ maxSizeMb }}MB，尺寸不超过 {{ maxWidth }} x {{ maxHeight }}</div>
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
  tip?: string
  maxSizeMb?: number
  maxWidth?: number
  maxHeight?: number
}>(), {
  modelValue: '',
  keyPrefix: 'uploads',
  tip: '支持 jpg/png/webp，使用通用 OSS 上传接口',
  maxSizeMb: 2,
  maxWidth: 1024,
  maxHeight: 1024,
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

  const meta = await loadImageMeta(file).catch(() => null)
  if (!meta) {
    ElMessage.warning('图片读取失败，请重新选择')
    input.value = ''
    return
  }
  if (meta.width > props.maxWidth || meta.height > props.maxHeight) {
    ElMessage.warning(`图片尺寸不能超过 ${props.maxWidth} x ${props.maxHeight}`)
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

function loadImageMeta(file: File): Promise<{ width: number; height: number }> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const image = new Image()
      image.onload = () => resolve({ width: image.width, height: image.height })
      image.onerror = reject
      image.src = String(reader.result)
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}
</script>

<style scoped>
.image-upload-field {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-box {
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

.actions-box {
  display: grid;
  gap: 8px;
}

.tip-text {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}

.hidden-input {
  display: none;
}
</style>
