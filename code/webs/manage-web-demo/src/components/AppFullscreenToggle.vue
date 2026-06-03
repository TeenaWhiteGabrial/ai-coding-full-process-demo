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
  border: none;
  border-radius: 12px;
  background: hsl(var(--card) / 0.72);
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition:
    background-color 0.18s ease,
    color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.header-action-btn:hover {
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--foreground));
  box-shadow: 0 10px 24px hsl(var(--shadow-soft));
  transform: translateY(-1px);
}

.header-action-btn:focus-visible {
  outline: none;
  box-shadow:
    0 0 0 2px hsl(var(--theme-primary) / 0.18),
    0 10px 24px hsl(var(--shadow-soft));
}
</style>
