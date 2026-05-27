<template>
  <div class="login-page">
    <img
      class="login-background"
      :src="backgroundIllustration"
      alt="illustration background"
    >
    <div class="login-overlay"></div>

    <div class="login-toolbar">
      <ThemeToggle />
    </div>

    <div class="login-shell">
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
import backgroundIllustration from '@/assets/login-background.jpg'

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
  height: 100vh;
  position: relative;
  overflow: hidden;
  display: grid;
  align-items: center;
  padding: 32px;
  background: hsl(var(--background-deep));
}

.login-background,
.login-overlay {
  position: absolute;
  inset: 0;
}

.login-background {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  filter: saturate(0.94) contrast(1.01);
}

.login-overlay {
  background:
    linear-gradient(90deg, hsl(210 22% 10% / 0.08) 0%, hsl(210 22% 10% / 0.04) 36%, hsl(210 22% 10% / 0.28) 100%),
    linear-gradient(180deg, hsl(0 0% 100% / 0.02), hsl(0 0% 0% / 0.06));
}

.login-toolbar {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 2;
}

.login-shell {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-end;
  width: min(1240px, 100%);
  height: 100%;
  margin: 0 auto;
}

.login-card {
  width: min(100%, 430px);
  border: 1px solid hsl(var(--border) / 0.75);
  border-radius: 32px;
  background:
    linear-gradient(180deg, hsl(var(--card) / 0.95), hsl(var(--secondary) / 0.9));
  box-shadow:
    0 28px 80px hsl(220 35% 10% / 0.24),
    inset 0 1px 0 hsl(0 0% 100% / 0.08);
  backdrop-filter: blur(20px);
  align-self: center;
}

.login-card :deep(.el-card__body) {
  padding: 30px 30px 24px;
}

.login-head {
  margin-bottom: 18px;
}

.login-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.login-head h2 {
  margin: 0;
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
  gap: 4px;
}

.submit-btn {
  width: 100%;
  min-height: 48px;
  margin-top: 6px;
  border-radius: 16px;
  font-weight: 700;
  box-shadow: 0 14px 28px hsl(var(--primary) / 0.2);
}

@media (max-width: 860px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    justify-content: center;
  }

  .login-overlay {
    background:
      linear-gradient(180deg, hsl(210 22% 10% / 0.2), hsl(210 22% 10% / 0.34)),
      linear-gradient(180deg, hsl(0 0% 100% / 0.03), hsl(0 0% 0% / 0.08));
  }

  .login-card :deep(.el-card__body) {
    padding: 24px 22px 20px;
  }

  .login-head h2 {
    font-size: 28px;
  }
}
</style>
