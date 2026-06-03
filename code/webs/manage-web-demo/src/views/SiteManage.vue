<template>
  <div class="manage-page">
    <el-card>
      <PageLoadingOverlay :loading="pageLoading" compact>
        <el-form :model="form" label-position="top" class="site-form">
          <div class="form-grid">
            <div class="form-section">
              <el-form-item label="网站名称">
                <el-input v-model="form.siteName" maxlength="100" show-word-limit placeholder="请输入网站名称" />
              </el-form-item>
            </div>

            <div class="form-section span-2">
              <el-form-item label="网站 Logo">
                <ImageUploadField v-model="form.logoUrl" key-prefix="site/logo" />
              </el-form-item>
            </div>

            <div class="form-section span-2">
              <el-form-item label="网站 Icon">
                <ImageUploadField v-model="form.iconUrl" key-prefix="site/icon" />
              </el-form-item>
            </div>
          </div>

          <div class="form-actions">
            <el-button @click="loadConfig">重置</el-button>
            <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
          </div>
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
  gap: 18px;
  max-width: 760px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.form-section.span-2 {
  grid-column: span 2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 4px;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-section.span-2 {
    grid-column: auto;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
