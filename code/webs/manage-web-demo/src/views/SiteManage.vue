<template>
  <div class="manage-page">
    <el-card>
      <template #header>
        <PageCardHeader
          kicker="Site Config"
          title="网站管理"
          description="统一维护公开站点信息，控制标题、图标、Logo 与对外展示描述。"
        />
      </template>

      <div class="form-shell">
        <PageLoadingOverlay :loading="pageLoading" compact>
          <el-form :model="form" label-position="top" class="site-form">
            <div class="form-grid">
              <el-form-item label="网站标题">
                <el-input v-model="form.siteName" maxlength="100" show-word-limit placeholder="请输入网站标题" />
              </el-form-item>

              <el-form-item label="网站描述">
                <el-input
                  v-model="form.siteDescription"
                  type="textarea"
                  :rows="4"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入网站描述"
                />
              </el-form-item>

              <el-form-item label="网站 Logo">
                <ImageUploadField v-model="form.logoUrl" key-prefix="site/logo" />
                <div class="field-tip">建议上传透明背景 Logo，用于后台头部与公开展示区域。</div>
              </el-form-item>

              <el-form-item label="网站 Icon">
                <ImageUploadField v-model="form.iconUrl" key-prefix="site/icon" />
                <div class="field-tip">建议上传方形小图标，可用于浏览器标签页或系统角标。</div>
              </el-form-item>

              <el-form-item label="页脚文案">
                <el-input v-model="form.footerText" maxlength="500" show-word-limit placeholder="请输入页脚文案" />
              </el-form-item>

              <el-form-item label="版权信息">
                <el-input v-model="form.footerCopyright" maxlength="300" show-word-limit placeholder="请输入版权信息" />
              </el-form-item>

              <el-form-item label="备案号">
                <el-input v-model="form.footerRecord" maxlength="200" show-word-limit placeholder="请输入备案号" />
              </el-form-item>
            </div>

            <div class="preview-panel">
              <div class="preview-card">
                <div class="preview-browser">
                  <img v-if="resolvedIconUrl" :src="resolvedIconUrl" alt="site icon" class="preview-icon">
                  <div v-else class="preview-icon placeholder">ICO</div>
                  <div class="preview-meta">
                    <strong>{{ form.siteName || '网站标题预览' }}</strong>
                    <span>{{ form.siteDescription || '这里会显示网站描述预览。' }}</span>
                  </div>
                </div>
                <div class="preview-hero">
                  <img v-if="resolvedLogoUrl" :src="resolvedLogoUrl" alt="site logo" class="preview-logo">
                  <div v-else class="preview-logo placeholder">LOGO</div>
                  <p>{{ form.footerText || '这里会显示网站页脚文案。' }}</p>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <el-button @click="loadConfig">重置</el-button>
              <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
            </div>
          </el-form>
        </PageLoadingOverlay>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUploadField from '@/components/ImageUploadField.vue'
import PageCardHeader from '@/components/PageCardHeader.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'
import { getSiteConfigForManagement, updateSiteConfig, type SiteConfig } from '@/api/site'
import { useMenuStore } from '@/stores/menu'

const pageLoading = ref(false)
const saving = ref(false)
const menuStore = useMenuStore()

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

.form-shell {
  min-height: 0;
}

.site-form {
  display: grid;
  gap: 28px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px 24px;
}

.field-tip {
  margin-top: 10px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.6;
}

.preview-panel {
  border: 1px solid hsl(var(--border));
  border-radius: 24px;
  padding: 22px;
  background:
    linear-gradient(135deg, hsl(var(--card)), hsl(var(--secondary) / 0.45));
}

.preview-card {
  display: grid;
  gap: 18px;
}

.preview-browser {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
  background: hsl(var(--background));
}

.preview-icon,
.preview-logo {
  object-fit: contain;
  background: hsl(var(--muted));
}

.preview-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

.preview-meta {
  display: grid;
  gap: 4px;
}

.preview-meta strong {
  font-size: 15px;
}

.preview-meta span,
.preview-hero p {
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}

.preview-hero {
  display: grid;
  justify-items: start;
  gap: 14px;
  padding: 24px;
  border-radius: 22px;
  background: linear-gradient(145deg, hsl(var(--primary) / 0.1), transparent 55%);
}

.preview-logo {
  width: 120px;
  height: 120px;
  border-radius: 24px;
}

.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: hsl(var(--muted-foreground));
  border: 1px dashed hsl(var(--border));
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
