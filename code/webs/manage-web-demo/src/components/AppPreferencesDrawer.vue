<template>
  <el-tooltip content="偏好设置" placement="bottom">
    <button class="header-action-btn" type="button" title="偏好设置" @click="visible = true">
      <el-icon :size="18" class="settings-icon"><Setting /></el-icon>
    </button>
  </el-tooltip>

  <el-drawer
    v-model="visible"
    size="420px"
    :with-header="false"
    append-to-body
    class="preferences-drawer"
  >
    <div class="preferences-wrap">
      <DialogHero title="界面偏好" />

      <section class="preferences-section">
        <div class="section-title">主题模式</div>
        <div class="mode-grid">
          <button
            v-for="item in modes"
            :key="item.value"
            class="mode-card"
            :class="{ active: themeStore.theme === item.value }"
            type="button"
            @click="themeStore.setTheme(item.value)"
          >
            <strong>{{ item.label }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </div>
      </section>

      <section class="preferences-section">
        <div class="section-title">主题色</div>
        <div class="color-grid">
          <button
            v-for="item in themeColors"
            :key="item.value"
            class="color-card"
            :class="{ active: themeStore.themeColor === item.value }"
            type="button"
            @click="themeStore.setThemeColor(item.value)"
          >
            <span class="swatch" :style="{ background: item.color }" />
            <strong>{{ item.label }}</strong>
          </button>
        </div>
      </section>

      <section class="preferences-section">
        <div class="section-title">侧边栏风格</div>
        <div class="mode-grid">
          <button
            v-for="item in sidebarStyles"
            :key="item.value"
            class="mode-card"
            :class="{ active: themeStore.sidebarStyle === item.value }"
            type="button"
            @click="themeStore.setSidebarStyle(item.value)"
          >
            <strong>{{ item.label }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </div>
      </section>

      <section class="preferences-section">
        <div class="section-title">布局细节</div>
        <div class="switch-list">
          <div class="switch-item">
            <div>
              <strong>显示面包屑</strong>
              <span>在页面标题上方显示当前路径。</span>
            </div>
            <el-switch :model-value="themeStore.showBreadcrumb" @change="value => themeStore.setShowBreadcrumb(Boolean(value))" />
          </div>
          <div class="switch-item">
            <div>
              <strong>毛玻璃顶栏</strong>
              <span>让顶栏拥有半透明和模糊效果。</span>
            </div>
            <el-switch :model-value="themeStore.headerGlass" @change="value => themeStore.setHeaderGlass(Boolean(value))" />
          </div>
        </div>
      </section>

      <section class="preferences-section">
        <div class="section-title">页面过渡</div>
        <div class="switch-list">
          <div class="switch-item">
            <div>
              <strong>启用切换动画</strong>
            </div>
            <el-switch :model-value="themeStore.pageTransitionEnabled" @change="value => themeStore.setPageTransitionEnabled(Boolean(value))" />
          </div>
        </div>
        <div class="mode-grid transition-grid">
          <button
            v-for="item in transitions"
            :key="item.value"
            class="mode-card"
            :class="{ active: themeStore.pageTransitionName === item.value }"
            type="button"
            :disabled="!themeStore.pageTransitionEnabled"
            @click="themeStore.setPageTransitionName(item.value)"
          >
            <strong>{{ item.label }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </div>
      </section>

      <section class="preferences-section">
        <div class="section-title">顶栏工具</div>
        <div class="switch-list">
          <div v-for="item in tools" :key="item.key" class="switch-item">
            <div>
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </div>
            <el-switch
              :model-value="themeStore[item.key]"
              @change="value => themeStore.setToolVisible(item.key, Boolean(value))"
            />
          </div>
        </div>
      </section>

      <div class="preferences-actions">
        <el-button @click="themeStore.resetPreferences()">恢复默认</el-button>
        <el-button type="primary" @click="visible = false">完成</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Setting } from '@element-plus/icons-vue'

import DialogHero from '@/components/DialogHero.vue'
import {
  useThemeStore,
  type SidebarStyle,
  type ThemeColor,
} from '@/stores/theme'

const visible = ref(false)
const themeStore = useThemeStore()

const modes = [
  { value: 'light', label: '浅色', description: '适合白天办公和投屏展示。' },
  { value: 'dark', label: '深色', description: '适合长时间盯屏和夜间使用。' },
] as const

const themeColors: Array<{ color: string; label: string; value: ThemeColor }> = [
  { value: 'default', label: '默认蓝', color: '#1677ff' },
  { value: 'violet', label: '紫罗兰', color: '#7c3aed' },
  { value: 'pink', label: '玫红', color: '#db2777' },
  { value: 'rose', label: '蔷薇', color: '#e11d48' },
  { value: 'sky-blue', label: '天青', color: '#0284c7' },
  { value: 'deep-blue', label: '深蓝', color: '#1d4ed8' },
  { value: 'green', label: '森林绿', color: '#16a34a' },
  { value: 'orange', label: '橙色', color: '#ea580c' },
  { value: 'slate', label: '石板灰', color: '#475569' },
]

const sidebarStyles: Array<{ description: string; label: string; value: SidebarStyle }> = [
  { value: 'dark', label: '浅色侧栏', description: '使用默认侧栏配置，保留更完整的导航层次。' },
  { value: 'semi-dark', label: '深色侧栏', description: '使用更深的导航背景，突出菜单层级。' },
]

const transitions = [
  { value: 'fade-slide', label: '推荐淡入滑动', description: '轻微位移配合淡入淡出。' },
  { value: 'fade', label: '简洁淡入', description: '更安静，适合强调内容本身。' },
  { value: 'fade-up', label: '向上浮现', description: '进入时更有上浮感，节奏更轻。' },
  { value: 'fade-down', label: '向下落入', description: '适合偏工具化的页面切换。' },
] as const

const tools = [
  { key: 'enableSearch', label: '快捷搜索', description: '显示搜索按钮和 Cmd/Ctrl + K 快捷键。' },
  { key: 'enableFullscreen', label: '全屏按钮', description: '显示进入和退出全屏入口。' },
  { key: 'enableThemeToggle', label: '主题切换', description: '显示明暗模式快速切换按钮。' },
] as const
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

.settings-icon {
  transition: transform 0.28s ease;
  transform-origin: center;
}

.header-action-btn:hover .settings-icon {
  transform: rotate(36deg);
}

.preferences-wrap {
  display: grid;
  gap: 24px;
  padding: 24px 22px;
}

.preferences-section {
  display: grid;
  gap: 14px;
}

.section-title {
  color: hsl(var(--foreground));
  font-size: 14px;
  font-weight: 700;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mode-card,
.color-card {
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
  background: hsl(var(--panel));
  cursor: pointer;
}

.mode-card {
  display: grid;
  gap: 6px;
  padding: 16px;
  text-align: left;
}

.mode-card span {
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}

.mode-card.active,
.color-card.active {
  border-color: hsl(var(--primary) / 0.35);
  box-shadow: var(--ai-glow-xs);
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.color-card {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 14px 10px;
}

.swatch {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.18);
}

.color-card strong {
  font-size: 12px;
}

.switch-list {
  display: grid;
  gap: 12px;
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid hsl(var(--border));
  border-radius: 16px;
  background: hsl(var(--panel));
}

.switch-item > div {
  display: grid;
  gap: 4px;
}

.switch-item span {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.6;
}

.preferences-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
