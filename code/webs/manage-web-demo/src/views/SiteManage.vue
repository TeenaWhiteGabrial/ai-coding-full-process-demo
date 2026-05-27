<template>
  <div class="manage-page">
    <el-card class="site-card">
      <template #header>
        <div class="page-title">网站管理</div>
      </template>

      <PageLoadingOverlay :loading="pageLoading" compact>
        <div class="site-layout">
          <el-form :model="form" label-position="top" class="site-form">
            <section class="form-panel">
              <div class="panel-head">
                <h3>基础配置</h3>
              </div>

              <el-form-item label="网站名称">
                <el-input v-model="form.siteName" maxlength="100" show-word-limit placeholder="请输入网站名称" />
              </el-form-item>
            </section>

            <section class="form-panel">
              <div class="panel-head">
                <h3>品牌资源</h3>
              </div>

              <div class="asset-grid">
                <el-form-item label="网站 Logo">
                  <ImageUploadField v-model="form.logoUrl" key-prefix="site/logo" />
                  <div class="field-tip">建议上传横向或方形品牌图，用于后台左上角展示。</div>
                </el-form-item>

                <el-form-item label="网站 Icon">
                  <ImageUploadField v-model="form.iconUrl" key-prefix="site/icon" />
                  <div class="field-tip">建议上传清晰的小尺寸方形图标，用于浏览器标签页。</div>
                </el-form-item>
              </div>
            </section>

            <div class="form-actions">
              <el-button @click="loadConfig">重置</el-button>
              <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
            </div>
          </el-form>

          <aside class="preview-panel">
            <div class="preview-head">
              <h3>应用效果</h3>
            </div>

            <div class="preview-browser">
              <div class="browser-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="browser-tab">
                <img v-if="resolvedIconUrl" :src="resolvedIconUrl" alt="site icon" class="browser-icon">
                <div v-else class="browser-icon placeholder">ICO</div>
                <span>{{ form.siteName || '网站名称预览' }}</span>
              </div>
            </div>

            <div class="preview-brand">
              <div class="brand-mark">
                <img v-if="resolvedLogoUrl" :src="resolvedLogoUrl" alt="site logo" class="brand-logo">
                <div v-else class="brand-logo placeholder">LOGO</div>
              </div>
              <div class="brand-copy">
                <strong>{{ form.siteName || '网站名称预览' }}</strong>
                <span>后台品牌区会直接使用这组配置。</span>
              </div>
            </div>

            <ul class="preview-notes">
              <li>网站名称会同步更新页面标题。</li>
              <li>网站 Logo 会应用到后台左上角品牌区。</li>
              <li>网站 Icon 会更新浏览器标签页图标。</li>
            </ul>
          </aside>
        </div>
      </PageLoadingOverlay>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUploadField from '@/components/ImageUploadField.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'
import { getSiteConfigForManagement, updateSiteConfig, type SiteConfig } from '@/api/site'
import { useMenuStore } from '@/stores/menu'
import { useSiteStore } from '@/stores/site'

const pageLoading = ref(false)
const saving = ref(false)
const menuStore = useMenuStore()
const siteStore = useSiteStore()

const form = reactive<SiteConfig>({
  siteName: '',
  siteDescription: '',
  logoUrl: '',
  iconUrl: '',
  footerText: '',
  footerCopyright: '',
  footerRecord: '',
})

const resolvedLogoUrl = computed(() => resolveAssetUrl(form.logoUrl))
const resolvedIconUrl = computed(() => resolveAssetUrl(form.iconUrl))

function assignForm(data?: Partial<SiteConfig>) {
  form.siteName = data?.siteName || ''
  form.siteDescription = data?.siteDescription || ''
  form.logoUrl = data?.logoUrl || ''
  form.iconUrl = data?.iconUrl || ''
  form.footerText = data?.footerText || ''
  form.footerCopyright = data?.footerCopyright || ''
  form.footerRecord = data?.footerRecord || ''
}

function resolveAssetUrl(url?: string) {
  if (!url) return ''
  if (/^(https?:)?\/\//.test(url)) return url
  if (url.startsWith('/uploads/')) return url
  return `${import.meta.env.BASE_URL}${url.replace(/^\//, '')}`
}

async function loadConfig() {
  pageLoading.value = true
  try {
    const data = await getSiteConfigForManagement()
    assignForm(data)
  } finally {
    pageLoading.value = false
  }
}

async function submit() {
  saving.value = true
  try {
    await updateSiteConfig({ ...form })
    siteStore.applySiteConfig({ ...form })
    await menuStore.fetchMenus(true)
    ElMessage.success('网站配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>

<style scoped>
.manage-page :deep(.el-card__body) {
  position: relative;
  min-height: calc(100vh - 228px);
}

.site-card {
  overflow: hidden;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: hsl(var(--foreground));
}

.site-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(300px, 0.9fr);
  gap: 24px;
  align-items: start;
}

.site-form {
  display: grid;
  gap: 20px;
}

.form-panel,
.preview-panel {
  padding: 24px;
  border: 1px solid hsl(var(--border));
  border-radius: 24px;
  background:
    linear-gradient(180deg, hsl(var(--card) / 0.98), hsl(var(--card) / 0.92));
  box-shadow: 0 20px 48px hsl(var(--shadow-soft));
}

.panel-head,
.preview-head {
  display: grid;
  gap: 8px;
  margin-bottom: 18px;
}

.panel-head h3,
.preview-head h3 {
  margin: 0;
  font-size: 20px;
}

.field-tip,
.preview-notes {
  margin: 0;
  color: hsl(var(--muted-foreground));
  line-height: 1.7;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.preview-panel {
  display: grid;
  gap: 18px;
  position: sticky;
  top: 24px;
}

.preview-browser {
  display: grid;
  gap: 14px;
  padding: 18px;
  border: 1px solid hsl(var(--border));
  border-radius: 22px;
  background: hsl(var(--panel));
}

.browser-dots {
  display: flex;
  gap: 8px;
}

.browser-dots span {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: hsl(var(--muted-foreground) / 0.28);
}

.browser-tab {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 56px;
  padding: 14px 16px;
  border-radius: 16px;
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  box-shadow: inset 0 0 0 1px hsl(var(--border));
}

.browser-icon,
.brand-logo {
  display: block;
  object-fit: cover;
}

.browser-icon {
  width: 24px;
  height: 24px;
  border-radius: 8px;
}

.preview-brand {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, hsl(var(--primary) / 0.12), transparent 36%),
    hsl(var(--background-deep));
  border: 1px solid hsl(var(--border));
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 78px;
  height: 78px;
  border-radius: 22px;
  background: hsl(var(--card));
  box-shadow: inset 0 0 0 1px hsl(var(--border));
  overflow: hidden;
  flex-shrink: 0;
}

.brand-logo {
  width: 100%;
  height: 100%;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 700;
}

.brand-copy {
  display: grid;
  gap: 6px;
}

.brand-copy strong {
  font-size: 20px;
  color: hsl(var(--foreground));
}

.brand-copy span {
  color: hsl(var(--muted-foreground));
  line-height: 1.7;
}

.preview-notes {
  display: grid;
  gap: 10px;
  padding-left: 18px;
}

@media (max-width: 960px) {
  .site-layout {
    grid-template-columns: 1fr;
  }

  .preview-panel {
    position: static;
  }
}

@media (max-width: 640px) {
  .asset-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions :deep(.el-button) {
    flex: 1;
  }

  .preview-brand {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
