<template>
  <div class="manage-page">
    <el-card>
      <PageLoadingOverlay :loading="pageLoading" compact>
        <el-form :model="form" label-position="top" class="site-form">
          <div class="site-head">
            <div>
              <h2>网站配置</h2>
              <p>维护站点基础信息、品牌资源和页脚备案内容。</p>
            </div>
            <div class="head-actions">
              <el-button @click="loadConfig">重置</el-button>
              <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
            </div>
          </div>

          <section class="site-section">
            <div class="section-title">基础信息</div>
            <div class="form-grid">
              <el-form-item label="网站名称">
                <el-input v-model="form.siteName" maxlength="100" show-word-limit placeholder="请输入网站名称" />
              </el-form-item>

              <el-form-item label="备案号">
                <el-input v-model="form.footerRecord" maxlength="200" show-word-limit placeholder="例如：京ICP备XXXXXXXX号" />
              </el-form-item>

              <el-form-item label="网站简介" class="span-2">
                <el-input
                  v-model="form.siteDescription"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入网站简介"
                />
              </el-form-item>
            </div>
          </section>

          <section class="site-section">
            <div class="section-title">品牌资源</div>
            <div class="asset-grid">
              <el-form-item label="网站 Logo">
                <ImageUploadField v-model="form.logoUrl" key-prefix="site/logo" />
              </el-form-item>

              <el-form-item label="网站 Icon">
                <ImageUploadField v-model="form.iconUrl" key-prefix="site/icon" />
              </el-form-item>
            </div>
          </section>

          <section class="site-section">
            <div class="section-title">页脚信息</div>
            <div class="form-grid">
              <el-form-item label="页脚文案">
                <el-input v-model="form.footerText" maxlength="500" show-word-limit placeholder="请输入页脚文案" />
              </el-form-item>

              <el-form-item label="版权信息">
                <el-input v-model="form.footerCopyright" maxlength="300" show-word-limit placeholder="请输入版权信息" />
              </el-form-item>
            </div>
          </section>
        </el-form>
      </PageLoadingOverlay>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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

function assignForm(data?: Partial<SiteConfig>) {
  form.siteName = data?.siteName || ''
  form.siteDescription = data?.siteDescription || ''
  form.logoUrl = data?.logoUrl || ''
  form.iconUrl = data?.iconUrl || ''
  form.footerText = data?.footerText || ''
  form.footerCopyright = data?.footerCopyright || ''
  form.footerRecord = data?.footerRecord || ''
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

.site-form {
  display: grid;
  gap: 16px;
  min-width: 0;
  max-width: 920px;
}

.site-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 2px;
}

.site-head h2 {
  margin: 0;
  color: hsl(var(--foreground));
  font-size: 18px;
  font-weight: 700;
}

.site-head p {
  margin: 6px 0 0;
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}

.head-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 12px;
}

.site-section {
  padding: 18px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card));
}

.section-title {
  margin-bottom: 16px;
  color: hsl(var(--foreground));
  font-size: 15px;
  font-weight: 700;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.span-2 {
  grid-column: 1 / -1;
}

@media (max-width: 640px) {
  .form-grid,
  .asset-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }

  .site-head {
    flex-direction: column;
  }

  .head-actions {
    width: 100%;
    justify-content: stretch;
  }

  .head-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
