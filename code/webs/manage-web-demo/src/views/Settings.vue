<template>
  <div class="settings-page">
    <PageLoadingOverlay :loading="pageLoading">
      <div class="settings-layout">
        <el-card class="account-card">
          <div class="account-card__main">
            <div class="avatar-ring">
              <el-avatar :size="88" :src="userStore.userInfo?.avatar" icon="UserFilled" />
            </div>
            <h2>{{ displayName }}</h2>
            <p>{{ userStore.userInfo?.username || '-' }}</p>
            <el-tag :type="userStore.userInfo ? 'success' : 'info'" round>
              {{ userStore.userInfo ? '在线账号' : '未加载' }}
            </el-tag>
          </div>

          <el-divider />

          <div class="account-meta">
            <div>
              <span>账号</span>
              <strong>{{ userStore.userInfo?.username || '-' }}</strong>
            </div>
            <div>
              <span>姓名</span>
              <strong>{{ userStore.userInfo?.realName || '-' }}</strong>
            </div>
            <div>
              <span>邮箱</span>
              <strong>{{ userStore.userInfo?.email || '-' }}</strong>
            </div>
          </div>
        </el-card>

        <div class="settings-main">
          <el-card class="settings-panel">
            <template #header>
              <div class="panel-head">
                <div>
                  <h3>个人资料</h3>
                  <p>维护头像、姓名和邮箱信息。</p>
                </div>
              </div>
            </template>

            <el-form :model="form" label-position="top" class="settings-form">
              <el-form-item label="头像">
                <ImageUploadField
                  v-model="form.avatar"
                  key-prefix="avatars"
                  @uploaded="handleAvatarUploaded"
                />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model="form.realName" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
              </div>
            </el-form>
          </el-card>

          <el-card class="settings-panel">
            <template #header>
              <div class="panel-head">
                <div>
                  <h3>账号安全</h3>
                  <p>定期更新密码可以提升账号安全性。</p>
                </div>
                <el-button link type="primary" @click="togglePasswordForm">
                  {{ passwordVisible ? '收起' : '修改密码' }}
                </el-button>
              </div>
            </template>

            <div v-if="!passwordVisible" class="security-placeholder">
              当前账号密码不会明文展示。如需调整，请点击右上角“修改密码”。
            </div>

            <el-form v-else :model="passwordForm" label-position="top" class="settings-form">
              <el-form-item label="当前密码">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认新密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" plain :loading="passwordSaving" @click="changePassword">更新密码</el-button>
              </div>
            </el-form>
          </el-card>
        </div>
      </div>
    </PageLoadingOverlay>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import ImageUploadField from '@/components/ImageUploadField.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const pageLoading = ref(false)
const saving = ref(false)
const passwordSaving = ref(false)
const passwordVisible = ref(false)
const form = reactive({
  realName: userStore.userInfo?.realName || '',
  email: userStore.userInfo?.email || '',
  avatar: userStore.userInfo?.avatar || '',
})
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const displayName = computed(() =>
  userStore.userInfo?.realName || userStore.userInfo?.username || '用户',
)

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

function togglePasswordForm() {
  passwordVisible.value = !passwordVisible.value
  if (!passwordVisible.value) {
    resetPasswordForm()
  }
}

async function loadProfile() {
  pageLoading.value = true
  try {
    await userStore.refreshUserInfo()
    form.realName = userStore.userInfo?.realName || ''
    form.email = userStore.userInfo?.email || ''
    form.avatar = userStore.userInfo?.avatar || ''
  } finally {
    pageLoading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const res = await authApi.updateProfile(form) as any
    if (res.code === 200) {
      await userStore.refreshUserInfo()
      ElMessage.success('资料已保存')
    }
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请完整填写密码字段')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSaving.value = true
  try {
    const res = await authApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    }) as any
    if (res.code === 200) {
      resetPasswordForm()
      passwordVisible.value = false
      ElMessage.success('密码已更新')
    }
  } finally {
    passwordSaving.value = false
  }
}

async function handleAvatarUploaded(value: string) {
  form.avatar = value
  const saveRes = await authApi.updateProfile({ avatar: form.avatar }) as any
  if (saveRes.code === 200) {
    await userStore.refreshUserInfo()
    ElMessage.success('头像已更新')
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.settings-page {
  min-width: 0;
}

.settings-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.account-card {
  position: sticky;
  top: 18px;
}

.account-card :deep(.el-card__body) {
  padding: 24px;
}

.account-card__main {
  display: grid;
  justify-items: center;
  text-align: center;
}

.avatar-ring {
  display: inline-flex;
  padding: 8px;
  margin-bottom: 14px;
  background: hsl(var(--theme-primary-subtle));
  border-radius: 50%;
}

.account-card__main h2 {
  max-width: 100%;
  margin: 0;
  overflow: hidden;
  font-size: 22px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-card__main p {
  margin: 8px 0 14px;
  color: hsl(var(--muted-foreground));
}

.account-meta {
  display: grid;
  gap: 14px;
}

.account-meta div {
  display: grid;
  gap: 5px;
}

.account-meta span,
.panel-head p {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.account-meta strong {
  min-width: 0;
  overflow: hidden;
  color: hsl(var(--foreground));
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.settings-main {
  display: grid;
  gap: 18px;
  min-width: 0;
}

.settings-panel :deep(.el-card__header) {
  padding: 18px 20px;
}

.settings-panel :deep(.el-card__body) {
  padding: 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
}

.panel-head p {
  margin: 6px 0 0;
}

.settings-form {
  display: grid;
  max-width: 680px;
}

.security-placeholder {
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  line-height: 1.8;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 2px;
}

@media (max-width: 960px) {
  .settings-layout {
    grid-template-columns: 1fr;
  }

  .account-card {
    position: static;
  }
}

@media (max-width: 640px) {
  .account-card :deep(.el-card__body),
  .settings-panel :deep(.el-card__body) {
    padding: 18px;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>
