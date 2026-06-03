<template>
  <div class="tabbar-shell">
    <div class="tabbar-track">
      <div class="tabbar-list">
        <div
          v-for="(tab, index) in tabbarStore.tabs"
          :key="tab.fullPath"
          class="tabbar-item"
          :class="{ active: activePath === tab.fullPath, fixed: tab.fixed }"
          role="button"
          tabindex="0"
          @click="goToTab(tab.fullPath)"
          @keydown.enter.prevent="goToTab(tab.fullPath)"
          @keydown.space.prevent="goToTab(tab.fullPath)"
          @contextmenu.prevent="openContextMenu($event, tab.fullPath)"
        >
          <span
            v-if="index !== 0 && activePath !== tab.fullPath"
            class="tabbar-divider"
          ></span>

          <div class="tabbar-background">
            <span class="tabbar-curve tabbar-curve-left"></span>
            <span class="tabbar-curve tabbar-curve-right"></span>
            <div class="tabbar-background-content"></div>
          </div>

          <div class="tabbar-main">
            <el-icon v-if="tab.fixed" :size="12" class="tabbar-pin">
              <StarFilled />
            </el-icon>
            <span class="tabbar-title">{{ tab.title }}</span>
          </div>

          <button
            v-if="tab.closable"
            class="tabbar-close"
            type="button"
            title="关闭标签"
            @click.stop="closeTab(tab.fullPath)"
          >
            <el-icon :size="12"><Close /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <div class="tabbar-tools">
      <button class="tabbar-tool" type="button" title="刷新当前" @click="refreshCurrent(activePath)">
        <el-icon :size="15"><RefreshRight /></el-icon>
      </button>

      <el-dropdown trigger="click" @command="handleDropdownCommand">
        <button class="tabbar-tool" type="button" title="标签管理">
          <el-icon :size="15"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="refresh-current">刷新当前</el-dropdown-item>
            <el-dropdown-item command="close-current" :disabled="!activeTab?.closable">关闭当前</el-dropdown-item>
            <el-dropdown-item command="close-others">关闭其他</el-dropdown-item>
            <el-dropdown-item command="clear-all">关闭全部</el-dropdown-item>
            <el-dropdown-item divided disabled>最近访问</el-dropdown-item>
            <el-dropdown-item
              v-for="tab in tabbarStore.historyTabs.slice(0, 8)"
              :key="`history-${tab.fullPath}`"
              :command="`history:${tab.fullPath}`"
            >
              {{ tab.title }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>

  <teleport to="body">
    <div
      v-if="contextMenu.visible"
      class="tabbar-context-menu"
      :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
    >
      <button type="button" @click="handleContextCommand('refresh')">刷新当前页</button>
      <button type="button" @click="handleContextCommand('toggle-fixed')">
        {{ contextTarget?.fixed ? '取消固定标签' : '固定标签' }}
      </button>
      <button type="button" :disabled="!contextTarget?.closable" @click="handleContextCommand('close')">关闭当前</button>
      <button type="button" :disabled="isCloseLeftDisabled" @click="handleContextCommand('close-left')">关闭左侧</button>
      <button type="button" :disabled="isCloseRightDisabled" @click="handleContextCommand('close-right')">关闭右侧</button>
      <button type="button" @click="handleContextCommand('close-others')">关闭其他</button>
      <button type="button" @click="handleContextCommand('clear-all')">关闭全部</button>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Close, RefreshRight, StarFilled } from '@element-plus/icons-vue'

import { useAccessStore } from '@/stores/access'
import { useTabbarStore } from '@/stores/tabbar'

const route = useRoute()
const router = useRouter()
const accessStore = useAccessStore()
const tabbarStore = useTabbarStore()

const activePath = computed(() => route.fullPath)
const activeTab = computed(() =>
  tabbarStore.tabs.find(tab => tab.fullPath === activePath.value) || null,
)
const contextTarget = computed(() =>
  tabbarStore.tabs.find(tab => tab.fullPath === contextMenu.path) || null,
)
const contextIndex = computed(() =>
  tabbarStore.tabs.findIndex(tab => tab.fullPath === contextMenu.path),
)
const isCloseLeftDisabled = computed(() => {
  if (contextIndex.value <= 0) return true
  return !tabbarStore.tabs.slice(0, contextIndex.value).some(tab => !tab.fixed)
})
const isCloseRightDisabled = computed(() => {
  if (contextIndex.value === -1) return true
  return !tabbarStore.tabs.slice(contextIndex.value + 1).some(tab => !tab.fixed)
})

const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  path: '',
})

async function goToTab(path: string) {
  await router.push(path)
}

async function closeTab(path: string) {
  const isCurrent = activePath.value === path
  tabbarStore.removeTab(path)
  if (isCurrent) {
    await router.push(tabbarStore.getLastClosableTab(path).fullPath)
  }
}

function openContextMenu(event: MouseEvent, path: string) {
  contextMenu.visible = true
  contextMenu.x = Math.min(event.clientX, window.innerWidth - 176)
  contextMenu.y = Math.min(event.clientY, window.innerHeight - 220)
  contextMenu.path = path
}

function closeContextMenu() {
  contextMenu.visible = false
}

async function refreshCurrent(path: string) {
  tabbarStore.refreshTab(path)
  if (activePath.value !== path) {
    await router.push(path)
  }
}

async function handleContextCommand(command: string) {
  const path = contextMenu.path
  closeContextMenu()

  if (!path) return

  if (command === 'refresh') {
    await refreshCurrent(path)
    return
  }

  if (command === 'toggle-fixed') {
    tabbarStore.toggleFixed(path)
    return
  }

  if (command === 'close') {
    await closeTab(path)
    return
  }

  if (command === 'close-others') {
    tabbarStore.removeOtherTabs(path)
    if (activePath.value !== path) {
      await router.push(path)
    }
    return
  }

  if (command === 'close-left') {
    tabbarStore.removeLeftTabs(path)
    return
  }

  if (command === 'close-right') {
    tabbarStore.removeRightTabs(path)
    return
  }

  if (command === 'clear-all') {
    tabbarStore.clearTabs()
    await router.push(accessStore.homePath)
  }
}

async function handleDropdownCommand(command: string) {
  if (command.startsWith('history:')) {
    await goToTab(command.slice('history:'.length))
    return
  }

  if (command === 'refresh-current') {
    await refreshCurrent(activePath.value)
    return
  }

  if (command === 'close-current' && activeTab.value?.closable) {
    await closeTab(activePath.value)
    return
  }

  if (command === 'close-others') {
    tabbarStore.removeOtherTabs(activePath.value)
    return
  }

  if (command === 'clear-all') {
    tabbarStore.clearTabs()
    await router.push(accessStore.homePath)
  }
}

function handleGlobalClick() {
  if (contextMenu.visible) {
    closeContextMenu()
  }
}

onMounted(() => {
  window.addEventListener('click', handleGlobalClick)
})

onBeforeUnmount(() => {
  window.removeEventListener('click', handleGlobalClick)
})
</script>

<style scoped>
.tabbar-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: stretch;
  gap: 8px;
  min-height: 34px;
  padding: 6px 14px 0;
  border-bottom: 1px solid hsl(var(--border) / 0.72);
  background:
    linear-gradient(180deg, hsl(var(--header) / 0.7), hsl(var(--header) / 0.45));
}

.tabbar-track {
  min-width: 0;
  overflow: hidden;
}

.tabbar-list {
  display: flex;
  align-items: flex-end;
  min-width: 0;
  height: 100%;
  overflow: auto hidden;
  padding-right: 10px;
}

.tabbar-item {
  position: relative;
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  height: 28px;
  margin-right: -10px;
  padding: 0 14px 0 12px;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  user-select: none;
  transition: color 0.18s ease;
}

.tabbar-item:focus-visible {
  outline: none;
}

.tabbar-item.active {
  z-index: 3;
  color: hsl(var(--primary));
}

.tabbar-item.fixed .tabbar-title {
  font-weight: 700;
}

.tabbar-item:hover:not(.active) {
  color: hsl(var(--foreground));
}

.tabbar-item:hover:not(.active) + .tabbar-item .tabbar-divider,
.tabbar-item.active + .tabbar-item .tabbar-divider,
.tabbar-item:hover .tabbar-divider {
  opacity: 0;
}

.tabbar-divider {
  position: absolute;
  top: 50%;
  left: 8px;
  z-index: 0;
  width: 1px;
  height: 14px;
  background: hsl(var(--border));
  transform: translateY(-50%);
  transition: opacity 0.18s ease;
}

.tabbar-background {
  position: absolute;
  inset: 0 6px 0 6px;
  z-index: -1;
  opacity: 0.9;
}

.tabbar-background-content {
  height: 100%;
  border-radius: 10px 10px 0 0;
  background: transparent;
  transition:
    background-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.tabbar-curve {
  position: absolute;
  bottom: 0;
  width: 6px;
  height: 6px;
  background: transparent;
  transition: background-color 0.18s ease;
}

.tabbar-curve-left {
  left: 0;
  clip-path: path('M 6 6 L 6 0 Q 6 6 0 6 Z');
}

.tabbar-curve-right {
  right: 0;
  clip-path: path('M 0 0 L 0 6 Q 6 6 0 0 Z');
}

.tabbar-item:hover:not(.active) .tabbar-background-content {
  background: var(--theme-primary-subtle);
  transform: translateY(1px);
}

.tabbar-item.active .tabbar-background-content {
  background: linear-gradient(
    180deg,
    var(--theme-primary-soft-strong),
    var(--theme-primary-subtle)
  );
  box-shadow:
    inset 0 1px 0 hsl(var(--primary) / 0.18),
    0 10px 24px hsl(var(--shadow-soft));
}

.tabbar-item.active .tabbar-curve-left,
.tabbar-item.active .tabbar-curve-right {
  background: var(--theme-primary-subtle);
}

.tabbar-main {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding-right: 16px;
}

.tabbar-pin {
  color: hsl(var(--primary));
}

.tabbar-title {
  max-width: 156px;
  overflow: hidden;
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tabbar-close {
  position: absolute;
  top: 50%;
  right: 14px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transform: translateY(-50%);
  transition:
    background-color 0.18s ease,
    color 0.18s ease;
}

.tabbar-close:hover {
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--foreground));
}

.tabbar-tools {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 4px;
}

.tabbar-tool {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid hsl(var(--border));
  border-radius: 10px;
  background: hsl(var(--card));
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    background-color 0.18s ease,
    color 0.18s ease;
}

.tabbar-tool:hover {
  border-color: hsl(var(--primary) / 0.18);
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--primary));
}

.tabbar-context-menu {
  position: fixed;
  z-index: 100;
  display: grid;
  min-width: 168px;
  padding: 8px;
  border: 1px solid hsl(var(--border));
  border-radius: 14px;
  background: hsl(var(--card));
  box-shadow: 0 24px 56px hsl(var(--shadow-strong));
  backdrop-filter: blur(18px);
}

.tabbar-context-menu button {
  padding: 10px 12px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: hsl(var(--foreground));
  text-align: left;
  cursor: pointer;
}

.tabbar-context-menu button:hover:not(:disabled) {
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--primary));
}

.tabbar-context-menu button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

:deep(.el-dropdown-menu) {
  background: hsl(var(--card));
  border-color: hsl(var(--border));
}

:deep(.el-dropdown-menu__item) {
  color: hsl(var(--foreground));
}

:deep(.el-dropdown-menu__item:hover) {
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--primary));
}

:deep(.el-dropdown-menu__item.is-disabled) {
  color: hsl(var(--muted-foreground));
}

@media (max-width: 960px) {
  .tabbar-shell {
    padding: 6px 12px 0;
  }

  .tabbar-title {
    max-width: 108px;
  }
}
</style>
