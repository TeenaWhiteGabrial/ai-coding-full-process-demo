<template>
  <section class="fallback-page">
    <Icon404 class="fallback-illustration" />

    <div class="fallback-copy">
      <div class="fallback-code">404</div>
      <h1>哎呀！未找到页面</h1>
      <p>抱歉，我们无法找到你要访问的页面。它可能已经被移除，或者链接地址填写有误。</p>
      <el-button type="primary" size="large" @click="router.push(accessStore.homePath)">
        回到首页
      </el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import Icon404 from './components/Icon404.vue'

import { useAccessStore } from '@/stores/access'
import { useRouter } from 'vue-router'

const accessStore = useAccessStore()
const router = useRouter()
</script>

<style scoped>
.fallback-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 24px;
  text-align: center;
  background:
    radial-gradient(circle at top, hsl(var(--primary) / 0.1), transparent 28%),
    hsl(var(--background));
}

.fallback-illustration {
  width: min(100%, 520px);
  height: auto;
}

.fallback-copy {
  display: grid;
  gap: 14px;
  width: min(100%, 560px);
}

.fallback-code {
  color: hsl(var(--primary));
  font-size: 72px;
  line-height: 1;
  font-weight: 700;
}

.fallback-copy h1 {
  margin: 0;
  color: hsl(var(--foreground));
  font-size: clamp(30px, 4vw, 38px);
  line-height: 1.2;
}

.fallback-copy p {
  margin: 0 auto;
  color: hsl(var(--muted-foreground));
  max-width: 36rem;
  line-height: 1.8;
}

@media (max-width: 640px) {
  .fallback-page {
    padding: 24px 16px;
  }

  .fallback-illustration {
    width: min(100%, 360px);
  }

  .fallback-code {
    font-size: 56px;
  }
}
</style>
