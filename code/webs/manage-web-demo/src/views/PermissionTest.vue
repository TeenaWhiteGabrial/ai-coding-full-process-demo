<template>
  <div class="permission-page">
    <section class="hero-card">
      <div>
        <span class="section-kicker">Permission Test</span>
        <h1>页面权限与按钮权限测试</h1>
        <p>这个页面用于验证菜单可见性、动态路由访问，以及按钮级权限控制是否按照当前账号的权限集合正确生效。</p>
      </div>
      <div class="hero-badge">
        <strong>{{ permissions.length }}</strong>
        <span>当前按钮权限数</span>
      </div>
    </section>

    <section class="content-grid">
      <el-card class="permission-card">
        <template #header>
          <div class="panel-head">
            <div>
              <span class="panel-kicker">Current User</span>
              <h3>当前账号权限概览</h3>
            </div>
          </div>
        </template>

        <div class="info-list">
          <div class="info-item">
            <span>账号</span>
            <strong>{{ userStore.userInfo?.username || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>角色</span>
            <strong>{{ (userStore.userInfo?.roles || []).join(' / ') || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>页面权限标识</span>
            <strong>{{ hasPagePermission ? '已具备' : '未具备' }}</strong>
          </div>
        </div>
      </el-card>

      <el-card class="permission-card">
        <template #header>
          <div class="panel-head">
            <div>
              <span class="panel-kicker">Button Access</span>
              <h3>按钮权限测试区</h3>
            </div>
          </div>
        </template>

        <div class="button-grid">
          <el-button
            v-access="{ permissions: ['permission:test:view'] }"
            type="primary"
            @click="ElMessage.success('你拥有查看数据按钮权限')"
          >
            查看数据按钮
          </el-button>

          <el-button
            v-access="{ permissions: ['permission:test:danger'] }"
            type="danger"
            @click="ElMessage.warning('你拥有危险操作按钮权限')"
          >
            危险操作按钮
          </el-button>

          <el-button
            v-if="!permissions.includes('permission:test:view')"
            type="info"
            plain
            disabled
          >
            当前账号看不到“查看数据按钮”
          </el-button>

          <el-button
            v-if="!permissions.includes('permission:test:danger')"
            type="info"
            plain
            disabled
          >
            当前账号看不到“危险操作按钮”
          </el-button>
        </div>
      </el-card>

      <el-card class="permission-card full-span">
        <template #header>
          <div class="panel-head">
            <div>
              <span class="panel-kicker">Permission Codes</span>
              <h3>当前账号权限标识列表</h3>
            </div>
          </div>
        </template>

        <div class="permission-tags">
          <el-tag v-for="permission in permissions" :key="permission" class="tag-item">
            {{ permission }}
          </el-tag>
          <span v-if="permissions.length === 0" class="empty-text">当前账号没有按钮权限标识。</span>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const permissions = computed(() => userStore.userInfo?.permissions || [])
const hasPagePermission = computed(() => permissions.value.includes('permission:test:page'))
</script>

<style scoped>
.permission-page {
  display: grid;
  gap: 18px;
}

.hero-card {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 28px 30px;
  border: 1px solid hsl(var(--border));
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, hsl(var(--primary) / 0.14), transparent 30%),
    linear-gradient(145deg, hsl(var(--card) / 0.98), hsl(var(--card) / 0.9));
  box-shadow: 0 20px 48px hsl(var(--shadow-soft));
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

.hero-card h1,
.panel-head h3 {
  margin: 10px 0 8px;
}

.hero-card p,
.empty-text {
  margin: 0;
  color: hsl(var(--muted-foreground));
  line-height: 1.75;
}

.hero-badge {
  display: grid;
  gap: 6px;
  min-width: 140px;
  padding: 18px 20px;
  border-radius: 22px;
  background: hsl(var(--panel));
  text-align: center;
}

.hero-badge strong {
  font-size: 28px;
  color: hsl(var(--foreground));
}

.hero-badge span {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.permission-card {
  border-radius: 24px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-list,
.button-grid,
.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.info-item {
  display: grid;
  gap: 6px;
  min-width: 180px;
  padding: 16px 18px;
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
  background: hsl(var(--panel));
}

.info-item span {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.tag-item {
  margin-right: 0;
}

.full-span {
  grid-column: 1 / -1;
}

@media (max-width: 960px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
