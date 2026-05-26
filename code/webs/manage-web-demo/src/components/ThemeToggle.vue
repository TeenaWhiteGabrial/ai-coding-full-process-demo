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
