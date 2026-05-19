<template>
  <div class="login-page">
    <div class="login-grid">
      <section class="hero-panel">
        <div class="hero-badge">AI Studio Template</div>
        <h1 class="hero-title">精简后台模板</h1>
        <p class="hero-copy">
          当前模板只保留登录、上传、站点信息和个人设置，方便你在这个基础上继续扩展业务。
        </p>
        <div class="hero-points">
          <div class="hero-point">
            <span class="point-dot"></span>
            <span>文件上传接口已打通</span>
          </div>
          <div class="hero-point">
            <span class="point-dot"></span>
            <span>站点配置可直接读取</span>
          </div>
          <div class="hero-point">
            <span class="point-dot"></span>
            <span>数据库使用独立 migration 初始化</span>
          </div>
        </div>
      </section>

      <el-card class="login-card">
        <div class="login-head">
          <span class="login-kicker">Console Access</span>
          <h2>登录后台</h2>
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { FormInstance } from 'element-plus'

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
    router.push('/console/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
}

.login-grid {
  display: grid;
  grid-template-columns: minmax(320px, 540px) minmax(320px, 460px);
  gap: 24px;
  width: min(1080px, 100%);
}

.hero-panel {
  padding: 36px;
  border-radius: 32px;
  background:
    linear-gradient(135deg, hsl(var(--sidebar)), hsl(var(--primary-strong))),
    hsl(var(--sidebar));
  color: white;
  box-shadow: 0 30px 70px hsl(var(--shadow-strong));
}

.hero-badge {
  display: inline-flex;
  padding: 8px 14px;
  margin-bottom: 22px;
  background: hsl(0 0% 100% / 0.12);
  border: 1px solid hsl(0 0% 100% / 0.16);
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
  color: hsl(210 40% 96% / 0.8);
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
  background: hsl(0 0% 100% / 0.08);
  border-radius: 18px;
}

.point-dot {
  width: 10px;
  height: 10px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: hsl(210 40% 96%);
  box-shadow: 0 0 0 6px hsl(0 0% 100% / 0.1);
}

.login-card {
  align-self: center;
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
