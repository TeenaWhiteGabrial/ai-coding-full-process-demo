<template>
  <section class="system-page">
    <div class="system-card">
      <span class="system-code">403</span>
      <h1>当前账号没有访问这个页面的权限</h1>
      <p>可以返回工作台，或者切换有权限的账号后再试一次。</p>
      <div class="system-actions">
        <el-button @click="router.back()">返回上一页</el-button>
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
    radial-gradient(circle at top, hsl(var(--primary) / 0.12), transparent 28%),
    hsl(var(--background-deep));
}

.system-card {
  width: min(100%, 640px);
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
  max-width: 32rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.8;
}

.system-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}

@media (max-width: 640px) {
  .system-card {
    padding: 28px 22px;
  }

  .system-actions {
    flex-direction: column;
  }
}
</style>
