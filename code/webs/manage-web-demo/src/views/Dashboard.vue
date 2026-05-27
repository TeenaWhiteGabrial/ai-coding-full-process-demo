<template>
  <PageLoadingOverlay :loading="pageLoading">
    <div class="dashboard-page">
      <section class="hero">
        <div class="hero-copy">
          <span class="section-kicker">Project Overview</span>
          <h1>{{ siteInfo.siteName || 'AI Studio' }} 管理系统</h1>
          <p>
            这是一个面向业务后台场景的管理端 Demo，当前已经接通登录认证、菜单权限、组织管理、用户角色管理和站点品牌配置，
            可以直接作为中后台项目的起步模板继续扩展。
          </p>

          <div class="hero-tags">
            <span>Vue 3</span>
            <span>Element Plus</span>
            <span>Spring Boot</span>
            <span>MySQL</span>
          </div>

          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/users')">进入用户管理</el-button>
            <el-button size="large" @click="router.push('/site')">配置网站品牌</el-button>
          </div>
        </div>

        <div class="hero-panel">
          <div class="hero-panel-head">
            <div class="hero-badge">已接入</div>
            <strong>{{ menuCount }} 个管理入口</strong>
          </div>

          <div class="hero-site-card">
            <div class="browser-row">
              <img v-if="siteIconUrl" :src="siteIconUrl" alt="site icon" class="site-icon">
              <div v-else class="site-icon placeholder">ICO</div>
              <div class="browser-copy">
                <strong>{{ siteInfo.siteName || '网站名称' }}</strong>
                <span>浏览器标题与品牌区会使用这组站点配置</span>
              </div>
            </div>

            <div class="brand-row">
              <img v-if="siteLogoUrl" :src="siteLogoUrl" alt="site logo" class="site-logo">
              <div v-else class="site-logo placeholder">LOGO</div>
              <div class="brand-copy">
                <strong>控制台品牌展示</strong>
                <span>网站管理页保存后，这里的名称、Logo 与 Icon 会实时联动。</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="stats-grid">
        <article class="mini-stat">
          <span class="stat-label">项目形态</span>
          <strong>独立后台模板</strong>
          <small>前后端可单独启动，也适合继续做业务扩展。</small>
        </article>

        <article class="mini-stat">
          <span class="stat-label">接口基础路径</span>
          <strong>/ai-studio/v1</strong>
          <small>当前管理端接口统一挂载在这一层路径下。</small>
        </article>

        <article class="mini-stat">
          <span class="stat-label">当前能力</span>
          <strong>{{ featureCount }} 项</strong>
          <small>包含认证、权限、组织、站点配置等基础后台能力。</small>
        </article>
      </section>

      <section class="content-grid">
        <el-card class="content-card">
          <template #header>
            <div class="panel-head">
              <div>
                <span class="panel-kicker">Capabilities</span>
                <h3>本项目已具备的后台能力</h3>
              </div>
            </div>
          </template>

          <div class="feature-list">
            <article v-for="item in features" :key="item.title" class="feature-item">
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </el-card>

        <el-card class="content-card">
          <template #header>
            <div class="panel-head">
              <div>
                <span class="panel-kicker">Quick Start</span>
                <h3>推荐的继续使用路径</h3>
              </div>
            </div>
          </template>

          <div class="quick-list">
            <button
              v-for="item in quickLinks"
              :key="item.path"
              class="quick-item"
              type="button"
              @click="router.push(item.path)"
            >
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
              <span>进入</span>
            </button>
          </div>
        </el-card>

        <el-card class="content-card full-span">
          <template #header>
            <div class="panel-head">
              <div>
                <span class="panel-kicker">Architecture</span>
                <h3>项目组成说明</h3>
              </div>
            </div>
          </template>

          <div class="architecture-grid">
            <article class="architecture-item">
              <span>前端</span>
              <strong>Vue 3 + Element Plus 管理端</strong>
              <p>当前控制台基于 Vue 3、Pinia、Vue Router 和 Element Plus 组织，已接入主题模式、动态菜单和品牌配置。</p>
            </article>

            <article class="architecture-item">
              <span>后端</span>
              <strong>Spring Boot 独立服务</strong>
              <p>后端负责认证、菜单、角色、组织、站点配置、文件上传等能力，对外统一暴露 REST 接口。</p>
            </article>

            <article class="architecture-item">
              <span>扩展方式</span>
              <strong>按模块继续叠加业务</strong>
              <p>你可以在现有用户、角色、菜单、组织和网站管理基础上继续新增业务菜单与页面，保持同一套权限链路。</p>
            </article>
          </div>
        </el-card>
      </section>
    </div>
  </PageLoadingOverlay>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'
import { useMenuStore } from '@/stores/menu'
import { useSiteStore } from '@/stores/site'

const pageLoading = ref(false)
const router = useRouter()
const menuStore = useMenuStore()
const siteStore = useSiteStore()

const siteInfo = computed(() => siteStore.config)
const siteLogoUrl = computed(() => siteStore.resolvedLogoUrl)
const siteIconUrl = computed(() => siteStore.resolvedIconUrl)
const menuCount = computed(() => countMenus(menuStore.menus))
const features = [
  {
    title: '登录与访问控制',
    description: '支持登录鉴权、用户信息读取、动态菜单装配和 403/404 异常页处理。',
  },
  {
    title: '用户、角色、菜单管理',
    description: '可维护账号、角色与菜单，并通过角色菜单关系控制控制台的可访问范围。',
  },
  {
    title: '组织管理',
    description: '已经支持树形组织结构、用户归属组织、组织启停用和按组织筛选用户。',
  },
  {
    title: '网站品牌配置',
    description: '支持维护网站名称、Logo、Icon，并实时应用到后台品牌区与浏览器标签页。',
  },
]
const quickLinks = [
  {
    path: '/users',
    title: '用户管理',
    description: '维护账号、状态、角色归属和组织归属。',
  },
  {
    path: '/roles',
    title: '角色管理',
    description: '配置角色与菜单权限关系。',
  },
  {
    path: '/orgs',
    title: '组织管理',
    description: '搭建组织树并维护组织启停用状态。',
  },
  {
    path: '/site',
    title: '网站管理',
    description: '维护控制台品牌名称、Logo 和 Icon。',
  },
]
const featureCount = features.length

onMounted(async () => {
  pageLoading.value = true
  try {
    await Promise.all([
      siteStore.fetchSiteConfig(),
      menuStore.fetchMenus(),
    ])
  } finally {
    pageLoading.value = false
  }
})

function countMenus(items: Array<{ children?: any[] }>): number {
  return items.reduce((total, item) => {
    return total + 1 + countMenus(item.children || [])
  }, 0)
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 20px;
  padding: 28px;
  border-radius: 28px;
  border: 1px solid hsl(var(--border));
  background:
    radial-gradient(circle at top right, hsl(var(--primary) / 0.16), transparent 28%),
    linear-gradient(145deg, hsl(var(--card) / 0.98), hsl(var(--card) / 0.88));
  box-shadow: 0 22px 56px hsl(var(--shadow-soft));
}

.section-kicker,
.panel-kicker,
.stat-label,
.architecture-item span {
  display: inline-block;
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-copy {
  display: grid;
  align-content: center;
  gap: 16px;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 44px);
  line-height: 1.12;
}

.hero-copy p,
.panel-head p,
.feature-item p,
.quick-item p,
.architecture-item p,
.brand-copy span,
.browser-copy span,
.mini-stat small {
  margin: 0;
  color: hsl(var(--muted-foreground));
  line-height: 1.75;
}

.hero-tags,
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-tags span {
  padding: 9px 14px;
  border: 1px solid hsl(var(--border));
  border-radius: 999px;
  background: hsl(var(--panel));
  color: hsl(var(--foreground));
  font-size: 13px;
  font-weight: 700;
}

.hero-panel {
  display: grid;
  gap: 16px;
  padding: 22px;
  border-radius: 24px;
  background: hsl(var(--panel));
  border: 1px solid hsl(var(--border));
}

.hero-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.hero-badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: hsl(var(--primary) / 0.1);
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.hero-site-card {
  display: grid;
  gap: 14px;
}

.browser-row,
.brand-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
}

.browser-copy,
.brand-copy {
  display: grid;
  gap: 4px;
}

.site-icon,
.site-logo {
  display: block;
  object-fit: cover;
  flex: 0 0 auto;
}

.site-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

.site-logo {
  width: 64px;
  height: 64px;
  border-radius: 18px;
}

.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 700;
}

.stats-grid,
.content-grid {
  display: grid;
  gap: 18px;
}

.stats-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.mini-stat {
  display: grid;
  gap: 8px;
  padding: 22px 24px;
  border: 1px solid hsl(var(--border));
  border-radius: 24px;
  background: hsl(var(--panel));
}

.mini-stat strong {
  font-size: 24px;
  color: hsl(var(--foreground));
}

.content-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.content-card,
.feature-item,
.quick-item,
.architecture-item {
  border-radius: 24px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-head h3 {
  margin: 10px 0 0;
}

.feature-list,
.quick-list,
.architecture-grid {
  display: grid;
  gap: 14px;
}

.feature-item,
.architecture-item {
  padding: 18px 20px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--panel));
}

.feature-item strong,
.quick-item strong,
.architecture-item strong,
.browser-copy strong,
.brand-copy strong {
  color: hsl(var(--foreground));
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  width: 100%;
  padding: 18px 20px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--panel));
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.quick-item:hover {
  border-color: hsl(var(--primary) / 0.32);
  transform: translateY(-1px);
  box-shadow: 0 14px 34px hsl(var(--shadow-soft));
}

.quick-item span {
  color: hsl(var(--primary));
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.full-span {
  grid-column: 1 / -1;
}

.architecture-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 1080px) {
  .hero,
  .content-grid,
  .architecture-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    padding: 22px;
  }

  .hero-actions,
  .hero-tags {
    flex-direction: column;
    align-items: stretch;
  }

  .brand-row,
  .browser-row,
  .quick-item {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
