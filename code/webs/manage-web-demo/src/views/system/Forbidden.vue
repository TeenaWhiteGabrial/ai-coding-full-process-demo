<template>
  <section class="fallback-page">
    <Icon403 class="fallback-illustration fallback-illustration-403" />

    <div class="fallback-copy">
      <div class="fallback-code">403</div>
      <h1>抱歉，你无权访问该页面</h1>
      <p>当前账号没有对应的访问权限。你可以返回上一页，或者切换有权限的账号后继续操作。</p>
      <div class="fallback-actions">
        <el-button size="large" @click="router.back()">返回上一页</el-button>
        <el-button type="primary" size="large" @click="router.push(accessStore.homePath)">
          回到首页
        </el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import Icon403 from './components/Icon403.vue'

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
  width: min(100%, 420px);
  height: auto;
}

.fallback-illustration-403 {
  width: min(100%, 360px);
}

.fallback-copy {
  display: grid;
  gap: 14px;
  width: min(100%, 620px);
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
  max-width: 38rem;
  line-height: 1.8;
}

.fallback-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 640px) {
  .fallback-page {
    padding: 24px 16px;
  }

  .fallback-illustration {
    width: min(100%, 300px);
  }

  .fallback-code {
    font-size: 56px;
  }

  .fallback-actions {
    flex-direction: column;
  }
}
</style>
