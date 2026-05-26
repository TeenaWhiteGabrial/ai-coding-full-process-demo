<template>
  <div class="settings-page">
    <PageLoadingOverlay :loading="pageLoading">
      <el-card class="profile-card">
        <div class="profile-head">
          <div class="avatar-badge">
            <el-avatar :size="72" :src="userStore.userInfo?.avatar" icon="UserFilled" />
          </div>
          <div>
            <span class="panel-kicker">个人信息</span>
            <h2>{{ userStore.userInfo?.realName || userStore.userInfo?.username || 'Administrator' }}</h2>
            <p>管理你的基础资料和登录密码。</p>
          </div>
        </div>
      </el-card>

      <div class="settings-grid">
        <el-card>
          <template #header>
            <div class="panel-head">
              <div>
                <span class="panel-kicker">基础资料</span>
                <h3>个人资料</h3>
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
              <el-input v-model="form.realName" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-button type="primary" @click="save">保存资料</el-button>
          </el-form>
        </el-card>

        <el-card>
          <template #header>
            <div class="panel-head">
              <div>
                <span class="panel-kicker">安全设置</span>
                <h3>修改密码</h3>
              </div>
            </div>
          </template>
          <el-form :model="passwordForm" label-position="top" class="settings-form">
            <el-form-item label="当前密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-button type="primary" plain @click="changePassword">更新密码</el-button>
          </el-form>
        </el-card>
      </div>
    </PageLoadingOverlay>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'
import { useUserStore } from '@/stores/user'
import ImageUploadField from '@/components/ImageUploadField.vue'

const userStore = useUserStore()
const pageLoading = ref(false)
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
  const res = await authApi.updateProfile(form) as any
  if (res.code === 200) {
    await userStore.refreshUserInfo()
    ElMessage.success('资料已保存')
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请完整填写密码字段')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  const res = await authApi.changePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
  }) as any
  if (res.code === 200) {
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码已更新')
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
  display: grid;
  gap: 18px;
}

.profile-card :deep(.el-card__body) {
  padding: 28px 30px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 18px;
}

.avatar-badge {
  display: inline-flex;
  padding: 10px;
  background: linear-gradient(135deg, hsl(var(--primary) / 0.18), hsl(var(--primary-strong) / 0.08));
  border-radius: 24px;
}

.panel-kicker {
  display: inline-block;
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.profile-head h2,
.panel-head h3 {
  margin: 8px 0 6px;
}

.profile-head p {
  margin: 0;
  color: hsl(var(--muted-foreground));
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.settings-form {
  display: grid;
}

@media (max-width: 960px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }

  .profile-head {
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .profile-head {
    flex-direction: column;
  }
}
</style>
