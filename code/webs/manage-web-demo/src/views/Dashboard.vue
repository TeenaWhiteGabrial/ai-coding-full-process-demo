<template>
  <PageLoadingOverlay :loading="pageLoading">
    <div class="dashboard-page">
      <section class="hero">
        <div class="hero-copy">
          <span class="section-kicker">Project Overview</span>
          <h1>{{ siteInfo.siteName || 'AI Studio' }} 管理系统</h1>
          <p>
            这是一个面向中后台场景的管理系统 Demo，当前已经具备登录认证、页面权限、按钮权限、组织管理、字典管理和网站品牌配置能力，
            可以直接作为业务管理端的基础底座继续扩展。
          </p>

          <div class="hero-tags">
            <span>Vue 3</span>
            <span>Element Plus</span>
            <span>Spring Boot</span>
            <span>MySQL</span>
          </div>

          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/users')">进入用户管理</el-button>
            <el-button size="large" @click="router.push('/dicts')">查看字典管理</el-button>
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
                <span>浏览器标题、品牌区和首页说明均会跟随网站配置实时联动。</span>
              </div>
            </div>

            <div class="brand-row">
              <img v-if="siteLogoUrl" :src="siteLogoUrl" alt="site logo" class="site-logo">
              <div v-else class="site-logo placeholder">LOGO</div>
              <div class="brand-copy">
                <strong>控制台品牌展示</strong>
                <span>网站管理保存后，Logo、Icon 和网站名称会同步应用到系统各处。</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="stats-grid">
        <article class="mini-stat">
          <span class="stat-label">项目形态</span>
          <strong>独立后台模板</strong>
          <small>支持权限、配置、品牌和业务模块按需继续叠加。</small>
        </article>

        <article class="mini-stat">
          <span class="stat-label">接口基础路径</span>
          <strong>/ai-studio/v1</strong>
          <small>当前控制台和公共接口均统一挂载在这一层路径下。</small>
        </article>

        <article class="mini-stat">
          <span class="stat-label">当前能力</span>
          <strong>{{ featureCount }} 项</strong>
          <small>包含认证、组织、菜单、字典、品牌配置等基础后台能力。</small>
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
                <span class="panel-kicker">Public APIs</span>
                <h3>公共接口说明</h3>
              </div>
            </div>
          </template>

          <div class="api-grid">
            <article class="api-card">
              <div class="api-head">
                <div>
                  <span class="api-method">GET</span>
                  <strong>/common/site/config</strong>
                </div>
                <el-tag type="success">网站信息</el-tag>
              </div>
              <p>获取网站名称、Logo、Icon 等公共站点信息，前台站点和后台品牌区都可以直接使用。</p>
              <div class="api-meta">
                <span>鉴权：无需登录</span>
                <span>用途：网站标题、页签图标、品牌展示</span>
              </div>
              <div class="api-preview">
                <div class="preview-item">
                  <span>网站名称</span>
                  <strong>{{ siteInfo.siteName || '-' }}</strong>
                </div>
                <div class="preview-item">
                  <span>Logo</span>
                  <strong>{{ siteInfo.logoUrl || '-' }}</strong>
                </div>
                <div class="preview-item">
                  <span>Icon</span>
                  <strong>{{ siteInfo.iconUrl || '-' }}</strong>
                </div>
              </div>
            </article>

            <article class="api-card">
              <div class="api-head">
                <div>
                  <span class="api-method">GET</span>
                  <strong>/common/dict/tree?dictType=industry</strong>
                </div>
                <el-tag>字典信息</el-tag>
              </div>
              <p>根据字典类型获取对应的树形字典数据。比如 `industry` 可返回船舶、新闻等一级行业及其子级数据。</p>
              <div class="api-meta">
                <span>鉴权：需登录</span>
                <span>示例参数：`dictType=industry`</span>
              </div>
              <div class="dict-preview">
                <div class="dict-preview-head">
                  <strong>示例返回</strong>
                  <span>{{ dictSummary }}</span>
                </div>
                <el-tag
                  v-for="item in dictRoots"
                  :key="item.id"
                  class="dict-tag"
                  effect="plain"
                >
                  {{ item.dictLabel }}
                </el-tag>
                <span v-if="dictRoots.length === 0" class="empty-text">当前没有读取到 `industry` 字典数据。</span>
              </div>
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

import { managementApi, type DictItem } from '@/api/management'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'
import { useMenuStore } from '@/stores/menu'
import { useSiteStore } from '@/stores/site'

const pageLoading = ref(false)
const router = useRouter()
const menuStore = useMenuStore()
const siteStore = useSiteStore()
const dictRoots = ref<DictItem[]>([])

const siteInfo = computed(() => siteStore.config)
const siteLogoUrl = computed(() => siteStore.resolvedLogoUrl)
const siteIconUrl = computed(() => siteStore.resolvedIconUrl)
const menuCount = computed(() => countMenus(menuStore.menus))
const dictSummary = computed(() => {
  const childCount = dictRoots.value.reduce((total, item) => total + (item.children?.length || 0), 0)
  return `${dictRoots.value.length} 个一级项 / ${childCount} 个子级项`
})

const features = [
  {
    title: '登录与访问控制',
    description: '支持验证码登录、失败锁定、动态菜单、页面权限和按钮权限控制。',
  },
  {
    title: '用户、角色、菜单管理',
    description: '可维护账号、角色、菜单和按钮权限，并通过权限链路控制控制台可见范围。',
  },
  {
    title: '组织与字典管理',
    description: '支持树形组织结构和树形字典结构，适合作为业务元数据管理基础。',
  },
  {
    title: '网站品牌配置',
    description: '支持维护网站名称、Logo、Icon，并同步应用到品牌区和浏览器标签页。',
  },
]

const quickLinks = [
  {
    path: '/users',
    title: '用户管理',
    description: '维护账号、状态、角色归属和组织归属。',
  },
  {
    path: '/menus',
    title: '菜单管理',
    description: '维护菜单、按钮和权限标识。',
  },
  {
    path: '/dicts',
    title: '字典管理',
    description: '维护一级字典和二级字典数据。',
  },
  {
    path: '/site',
    title: '网站管理',
    description: '维护网站名称、Logo 与 Icon。',
  },
]

const featureCount = features.length

onMounted(async () => {
  pageLoading.value = true
  try {
    const [dictRes] = await Promise.all([
      managementApi.dictTree('industry') as any,
      siteStore.fetchSiteConfig(),
      menuStore.fetchMenus(),
    ])
    dictRoots.value = dictRes.data || []
  } finally {
    pageLoading.value = false
  }
})

function countMenus(items: Array<{ children?: any[] }>): number {
  return items.reduce((total, item) => total + 1 + countMenus(item.children || []), 0)
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
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
.api-method {
  display: inline-block;
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero h1,
.panel-head h3 {
  margin: 10px 0 12px;
}

.hero-copy p,
.feature-item p,
.quick-item p,
.api-card p,
.empty-text {
  margin: 0;
  color: hsl(var(--muted-foreground));
  line-height: 1.75;
}

.hero-tags,
.hero-actions,
.api-meta,
.dict-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-tags {
  margin-top: 18px;
}

.hero-tags span,
.api-meta span {
  padding: 8px 12px;
  border-radius: 999px;
  background: hsl(var(--panel));
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.hero-actions {
  margin-top: 22px;
}

.hero-panel {
  display: grid;
  gap: 16px;
  align-content: start;
}

.hero-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.hero-badge {
  padding: 7px 12px;
  border-radius: 999px;
  background: hsl(var(--primary) / 0.12);
  color: hsl(var(--primary));
  font-weight: 700;
}

.hero-site-card,
.mini-stat,
.feature-item,
.quick-item,
.api-card {
  border: 1px solid hsl(var(--border));
  border-radius: 22px;
  background: hsl(var(--card));
}

.hero-site-card {
  display: grid;
  gap: 14px;
  padding: 18px;
}

.browser-row,
.brand-row,
.panel-head,
.api-head,
.dict-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.browser-copy,
.brand-copy {
  display: grid;
  gap: 6px;
}

.browser-copy span,
.brand-copy span {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.site-icon,
.site-logo,
.placeholder {
  display: grid;
  place-items: center;
  border-radius: 18px;
  background: hsl(var(--panel));
}

.site-icon,
.placeholder.site-icon {
  width: 56px;
  height: 56px;
  object-fit: cover;
}

.site-logo,
.placeholder.site-logo {
  width: 120px;
  height: 56px;
  object-fit: contain;
}

.placeholder {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 800;
}

.stats-grid,
.api-grid {
  display: grid;
  gap: 18px;
}

.stats-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.mini-stat {
  display: grid;
  gap: 10px;
  padding: 20px 22px;
}

.mini-stat strong {
  font-size: 22px;
}

.mini-stat small {
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.content-card {
  border-radius: 24px;
}

.full-span {
  grid-column: 1 / -1;
}

.feature-list,
.quick-list {
  display: grid;
  gap: 14px;
}

.feature-item,
.quick-item {
  padding: 18px 20px;
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.quick-item span:last-child {
  color: hsl(var(--primary));
  font-weight: 700;
}

.api-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.api-card {
  display: grid;
  gap: 14px;
  padding: 20px 22px;
}

.api-preview {
  display: grid;
  gap: 12px;
}

.preview-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  background: hsl(var(--panel));
}

.preview-item span {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.dict-preview {
  align-items: center;
}

.dict-tag {
  margin-right: 0;
}

@media (max-width: 1100px) {
  .hero,
  .content-grid,
  .api-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero,
  .mini-stat,
  .api-card {
    padding: 20px;
  }

  .hero-panel-head,
  .browser-row,
  .brand-row,
  .panel-head,
  .api-head,
  .dict-preview-head,
  .quick-item {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
