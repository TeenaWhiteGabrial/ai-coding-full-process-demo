<template>
  <button class="header-search-trigger" type="button" title="快捷搜索" @click="visible = true">
    <span class="header-search-leading">
      <el-icon :size="16"><Search /></el-icon>
    </span>
    <span class="header-search-text">搜索页面</span>
    <kbd>⌘K</kbd>
  </button>

  <el-dialog
    v-model="visible"
    width="640px"
    destroy-on-close
    append-to-body
    class="manage-editor-dialog command-dialog"
  >
    <template #header>
      <DialogHero title="快捷搜索" description="搜索控制台页面和常用管理入口，回车即可打开。" />
    </template>

    <el-input
      v-model="keyword"
      size="large"
      clearable
      placeholder="输入页面名称，例如 用户、角色、设置"
      @keyup.enter="openFirst"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <div class="search-results">
      <button
        v-for="item in filteredItems"
        :key="item.path"
        class="search-result-item"
        type="button"
        @click="navigate(item.path)"
      >
        <strong>{{ item.title }}</strong>
        <span>{{ item.description }}</span>
      </button>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

import DialogHero from '@/components/DialogHero.vue'
import { useMenuStore, type ConsoleMenu } from '@/stores/menu'

const router = useRouter()
const menuStore = useMenuStore()
const visible = ref(false)
const keyword = ref('')

interface SearchItem {
  description: string
  path: string
  title: string
}

function normalizePath(path?: string) {
  if (!path) return ''
  return path.replace(/^\/console(?=\/)/, '')
}

function collectSearchItems(menus: ConsoleMenu[], parentNames: string[] = []): SearchItem[] {
  return menus.flatMap((menu) => {
    if (menu.hidden === 1) return []

    const currentTrail = [...parentNames, menu.name]
    const path = normalizePath(menu.path)
    const current: SearchItem[] = path
      ? [{
        title: menu.name,
        path,
        description: currentTrail.join(' / '),
      }]
      : []

    return [...current, ...collectSearchItems(menu.children || [], currentTrail)]
  })
}

const items = computed(() => collectSearchItems(menuStore.menus))

const filteredItems = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return items.value
  return items.value.filter((item: SearchItem) =>
    `${item.title} ${item.description}`.toLowerCase().includes(text),
  )
})

async function navigate(path: string) {
  visible.value = false
  keyword.value = ''
  await router.push(path)
}

function openFirst() {
  const first = filteredItems.value[0]
  if (first) {
    navigate(first.path)
  }
}

function handleKeydown(event: KeyboardEvent) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    visible.value = true
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.header-search-trigger {
  height: 36px;
  min-width: 190px;
  padding: 0 10px 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: 1px solid hsl(var(--border));
  border-radius: 999px;
  background: hsl(var(--card) / 0.92);
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    background-color 0.18s ease,
    color 0.18s ease,
    transform 0.18s ease;
}

.header-search-trigger:hover {
  border-color: hsl(var(--primary) / 0.22);
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  transform: translateY(-1px);
}

.header-search-leading {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: hsl(var(--secondary));
  color: hsl(var(--foreground));
  flex: 0 0 auto;
}

.header-search-text {
  flex: 1;
  text-align: left;
  font-size: 13px;
  font-weight: 500;
}

.header-search-trigger kbd {
  padding: 1px 6px;
  border: 1px solid hsl(var(--border));
  border-radius: 999px;
  background: hsl(var(--panel));
  color: hsl(var(--muted-foreground));
  font-size: 11px;
  font-family: inherit;
  line-height: 1.6;
}

.search-results {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.search-result-item {
  width: 100%;
  display: grid;
  gap: 4px;
  padding: 16px;
  text-align: left;
  border: 1px solid hsl(var(--border));
  border-radius: 16px;
  background: hsl(var(--panel));
  cursor: pointer;
}

.search-result-item span {
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}

@media (max-width: 960px) {
  .header-search-trigger {
    min-width: 0;
    width: 38px;
    padding: 0;
    justify-content: center;
    border-radius: 12px;
  }

  .header-search-text,
  .header-search-trigger kbd {
    display: none;
  }
}
</style>
