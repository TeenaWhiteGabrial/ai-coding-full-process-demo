<template>
  <div class="login-page">
    <div class="login-toolbar">
      <ThemeToggle />
    </div>

    <div class="login-grid">
      <section class="hero-panel">
        <div class="hero-badge">Vue 3 + Element Plus</div>
        <h1 class="hero-title">AI Studio 管理后台</h1>
        <p class="hero-copy">
          这一版界面已经切到更接近 vben 的视觉语言，但登录、菜单、上传、站点设置和用户管理逻辑仍然沿用当前项目。
        </p>
        <div class="hero-points">
          <div class="hero-point">
            <span class="point-dot"></span>
            <span>业务接口与现有鉴权流程保持不变</span>
          </div>
          <div class="hero-point">
            <span class="point-dot"></span>
            <span>菜单、角色、用户等后台能力直接可用</span>
          </div>
          <div class="hero-point">
            <span class="point-dot"></span>
            <span>Element Plus 页面风格向 vben web-ele 靠拢</span>
          </div>
        </div>
      </section>

      <el-card class="login-card">
        <div class="login-head">
          <span class="login-kicker">Console Access</span>
          <h2>登录控制台</h2>
          <p>默认测试账号：`admin` / `admin123`</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="Username" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="Password" size="large" show-password />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
            进入控制台
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ThemeToggle from '@/components/ThemeToggle.vue'
import type { FormInstance } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: 'Username is required', trigger: 'blur' }],
  password: [{ required: true, message: 'Password is required', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.value.username, form.value.password)
    const redirect = typeof route.query.redirect === 'string'
      ? decodeURIComponent(route.query.redirect)
      : '/'
    router.push(redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  display: grid;
  place-items: center;
  padding: 28px;
  background:
    radial-gradient(circle at top left, hsl(var(--primary) / 0.12), transparent 28%),
    radial-gradient(circle at bottom right, hsl(var(--primary) / 0.1), transparent 24%),
    hsl(var(--background-deep));
}

.login-toolbar {
  position: absolute;
  top: 24px;
  right: 24px;
}

.login-grid {
  display: grid;
  grid-template-columns: minmax(340px, 580px) minmax(320px, 440px);
  gap: 26px;
  align-items: stretch;
  width: min(1120px, 100%);
}

.hero-panel {
  position: relative;
  overflow: hidden;
  padding: 42px;
  border: 1px solid hsl(var(--border));
  border-radius: 32px;
  background:
    linear-gradient(135deg, hsl(var(--sidebar)) 0%, hsl(var(--secondary)) 100%);
  color: hsl(var(--foreground));
  box-shadow: 0 30px 70px hsl(var(--shadow-strong));
}

.hero-panel::after {
  position: absolute;
  inset: auto -80px -100px auto;
  width: 260px;
  height: 260px;
  border-radius: 999px;
  background: radial-gradient(circle, hsl(var(--primary) / 0.18), transparent 66%);
  content: '';
}

.hero-badge {
  display: inline-flex;
  padding: 8px 14px;
  margin-bottom: 22px;
  background: hsl(var(--primary-soft));
  border: 1px solid hsl(var(--border));
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-title {
  margin: 0;
  font-size: clamp(36px, 5vw, 56px);
  line-height: 1.02;
}

.hero-copy {
  margin: 18px 0 28px;
  max-width: 32rem;
  color: hsl(var(--muted-foreground));
  font-size: 16px;
  line-height: 1.75;
}

.hero-points {
  display: grid;
  gap: 14px;
}

.hero-point {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: hsl(var(--card) / 0.7);
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
  backdrop-filter: blur(10px);
}

.point-dot {
  width: 10px;
  height: 10px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: hsl(var(--primary));
  box-shadow: 0 0 0 6px hsl(var(--primary) / 0.12);
}

.login-card {
  align-self: center;
  border-radius: 28px;
  box-shadow: 0 24px 64px hsl(var(--shadow-soft));
}

.login-card :deep(.el-card__body) {
  padding: 34px 32px 30px;
}

.login-head {
  margin-bottom: 24px;
}

.login-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.login-head h2 {
  margin: 0 0 10px;
  font-size: 30px;
  line-height: 1.08;
}

.login-head p {
  margin: 0;
  color: hsl(var(--muted-foreground));
  line-height: 1.7;
}

.login-form {
  display: grid;
  gap: 6px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  border-radius: 16px;
  font-weight: 700;
}

@media (max-width: 960px) {
  .login-grid {
    grid-template-columns: 1fr;
  }

  .login-toolbar {
    top: 16px;
    right: 16px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 16px;
  }

  .hero-panel,
  .login-card :deep(.el-card__body) {
    padding: 24px;
  }
}
</style>
