<template>
  <el-tooltip :content="isFullscreen ? '退出全屏' : '进入全屏'" placement="bottom">
    <button class="header-action-btn" type="button" :title="isFullscreen ? '退出全屏' : '进入全屏'" @click="toggleFullscreen">
      <el-icon :size="18">
        <FullScreen v-if="!isFullscreen" />
        <Aim v-else />
      </el-icon>
    </button>
  </el-tooltip>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { Aim, FullScreen } from '@element-plus/icons-vue'

const isFullscreen = ref(Boolean(document.fullscreenElement))

function syncState() {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

async function toggleFullscreen() {
  if (document.fullscreenElement) {
    await document.exitFullscreen()
  } else {
    await document.documentElement.requestFullscreen()
  }
  syncState()
}

onMounted(() => {
  document.addEventListener('fullscreenchange', syncState)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', syncState)
})
</script>

<style scoped>
.header-action-btn {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid hsl(var(--border));
  border-radius: 12px;
  background: hsl(var(--card) / 0.92);
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    background-color 0.18s ease,
    color 0.18s ease,
    transform 0.18s ease;
}

.header-action-btn:hover {
  border-color: hsl(var(--primary) / 0.22);
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  transform: translateY(-1px);
}
</style>
