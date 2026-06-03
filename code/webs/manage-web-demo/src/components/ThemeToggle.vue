<template>
  <el-tooltip :content="themeStore.theme === 'dark' ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
    <button class="header-action-btn" type="button" @click="toggle" :title="themeStore.theme === 'dark' ? '切换到浅色模式' : '切换到深色模式'">
      <el-icon v-if="themeStore.theme === 'dark'" :size="20">
        <Sunny />
      </el-icon>
      <el-icon v-else :size="20">
        <Moon />
      </el-icon>
    </button>
  </el-tooltip>
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'
import { Moon, Sunny } from '@element-plus/icons-vue'

const themeStore = useThemeStore()

function toggle() {
  themeStore.toggleTheme()
}
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
