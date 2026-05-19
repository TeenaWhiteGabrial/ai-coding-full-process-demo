<template>
  <div class="dashboard-page">
    <section class="hero">
      <div>
        <span class="section-kicker">Template Overview</span>
        <h1>基础能力已经就位</h1>
        <p>你现在可以直接测试文件上传、站点配置读取和账户配置修改。</p>
      </div>
      <el-button type="primary" size="large" class="hero-action" @click="triggerUpload">
        上传测试文件
      </el-button>
      <input ref="fileInput" type="file" style="display: none" @change="handleFileChange">
    </section>

    <section class="stats-grid">
      <article class="mini-stat">
        <span class="stat-label">已保留能力</span>
        <strong>4 项</strong>
        <small>登录、菜单、上传、站点信息</small>
      </article>
      <article class="mini-stat">
        <span class="stat-label">API 基础路径</span>
        <strong>/ai-studio/v1</strong>
        <small>前后端已经对齐</small>
      </article>
      <article class="mini-stat">
        <span class="stat-label">数据库</span>
        <strong>template_demo_v2</strong>
        <small>使用独立 migration 初始化</small>
      </article>
    </section>

    <section class="content-grid">
      <el-card class="upload-panel">
        <template #header>
          <div class="panel-head">
            <div>
              <span class="panel-kicker">Upload</span>
              <h3>文件上传测试</h3>
            </div>
          </div>
        </template>
        <p class="panel-copy">点击按钮上传任意文件，结果会展示返回的文件 URL 和对象键。</p>
        <el-button type="primary" @click="triggerUpload">选择文件</el-button>
        <div v-if="uploading" class="upload-state">正在上传，请稍候…</div>
        <div v-if="lastUpload" class="upload-result">
          <div class="result-row">
            <span>文件名</span>
            <strong>{{ lastUpload.fileName }}</strong>
          </div>
          <div class="result-row">
            <span>对象键</span>
            <code>{{ lastUpload.ossKey }}</code>
          </div>
          <div class="result-row">
            <span>访问地址</span>
            <a :href="lastUpload.ossUrl" target="_blank" rel="noreferrer">{{ lastUpload.ossUrl }}</a>
          </div>
        </div>
      </el-card>

      <el-card class="site-panel">
        <template #header>
          <div class="panel-head">
            <div>
              <span class="panel-kicker">Site Config</span>
              <h3>站点配置</h3>
            </div>
          </div>
        </template>
        <div class="info-list">
          <div class="info-item">
            <span>站点名称</span>
            <strong>{{ siteInfo.siteName || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>站点描述</span>
            <strong>{{ siteInfo.siteDescription || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>Logo 地址</span>
            <code>{{ siteInfo.logoUrl || '-' }}</code>
          </div>
          <div class="info-item">
            <span>Footer</span>
            <strong>{{ siteInfo.footerText || '-' }}</strong>
          </div>
        </div>
      </el-card>

      <el-card class="api-panel">
        <template #header>
          <div class="panel-head">
            <div>
              <span class="panel-kicker">API Endpoints</span>
              <h3>可直接联调的接口</h3>
            </div>
          </div>
        </template>
        <ul class="endpoint-list">
          <li><code>POST /common/auth/token</code></li>
          <li><code>GET /common/auth/user-info</code></li>
          <li><code>POST /common/oss/upload</code></li>
          <li><code>GET /common/site/config</code></li>
          <li><code>GET /common/menu/tree</code></li>
        </ul>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSiteConfig } from '@/api/site'
import { uploadApi } from '@/api/upload'

const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const siteInfo = ref({
  siteName: '',
  siteDescription: '',
  logoUrl: '',
  footerText: '',
})
const lastUpload = ref<null | {
  fileName: string
  ossKey: string
  ossUrl: string
}>(null)

onMounted(async () => {
  const config = await getSiteConfig()
  siteInfo.value = config
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadApi.upload(file) as any
    if (res.code === 200) {
      lastUpload.value = res.data
      ElMessage.success('上传成功')
    }
  } finally {
    uploading.value = false
    input.value = ''
  }
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 30px 32px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, hsl(var(--card) / 0.92), hsl(var(--card) / 0.72)),
    hsl(var(--card));
  border: 1px solid hsl(var(--border));
  box-shadow: 0 20px 50px hsl(var(--shadow-soft));
}

.section-kicker,
.panel-kicker {
  display: inline-block;
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero h1,
.panel-head h3 {
  margin: 10px 0 8px;
}

.hero p,
.panel-copy {
  margin: 0;
  max-width: 40rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.75;
}

.hero-action {
  border-radius: 16px;
  font-weight: 700;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.mini-stat {
  display: grid;
  gap: 6px;
  padding: 22px 24px;
  border: 1px solid hsl(var(--border));
  border-radius: 24px;
  background: hsl(var(--panel));
}

.stat-label {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.mini-stat strong {
  font-size: 24px;
}

.mini-stat small {
  color: hsl(var(--muted-foreground));
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 18px;
}

.api-panel {
  grid-column: 1 / -1;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.upload-state {
  margin-top: 16px;
  color: hsl(var(--primary));
  font-weight: 700;
}

.upload-result,
.info-list,
.endpoint-list {
  margin-top: 18px;
}

.upload-result,
.info-list {
  display: grid;
  gap: 14px;
}

.result-row,
.info-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  background: hsl(var(--panel-strong));
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
}

.result-row span,
.info-item span {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.result-row a {
  color: hsl(var(--primary));
  text-decoration: none;
  word-break: break-all;
}

.endpoint-list {
  display: grid;
  gap: 12px;
  padding-left: 18px;
}

.endpoint-list li {
  color: hsl(var(--foreground));
}

.endpoint-list code,
.result-row code,
.info-item code {
  font-family: "Cascadia Code", Consolas, monospace;
  word-break: break-all;
}

@media (max-width: 960px) {
  .stats-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
