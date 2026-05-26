<template>
  <section class="system-page">
    <div class="system-card">
      <span class="system-code">404</span>
      <h1>页面不存在或暂未接入控制台</h1>
      <p>如果这个页面原本应该存在，通常是菜单路径还没有和前端页面映射起来。</p>
      <div class="system-actions">
        <el-button type="primary" @click="router.push(accessStore.homePath)">回到首页</el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useAccessStore } from '@/stores/access'
import { useRouter } from 'vue-router'

const accessStore = useAccessStore()
const router = useRouter()
</script>

<style scoped>
.system-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at bottom left, hsl(var(--primary) / 0.12), transparent 32%),
    hsl(var(--background-deep));
}

.system-card {
  width: min(100%, 620px);
  padding: 40px;
  border: 1px solid hsl(var(--border));
  border-radius: 32px;
  background:
    linear-gradient(180deg, hsl(var(--card) / 0.95), hsl(var(--card) / 0.82));
  box-shadow: 0 32px 80px hsl(var(--shadow-soft));
  text-align: center;
}

.system-code {
  display: inline-block;
  color: hsl(var(--primary));
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.system-card h1 {
  margin: 16px 0 12px;
  font-size: clamp(28px, 4vw, 38px);
}

.system-card p {
  margin: 0 auto;
  max-width: 30rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.8;
}

.system-actions {
  margin-top: 28px;
}

@media (max-width: 640px) {
  .system-card {
    padding: 28px 22px;
  }
}
</style>
