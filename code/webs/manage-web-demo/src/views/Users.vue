<template>
  <div class="manage-page">
    <el-card>
      <div class="page-actions">
        <el-button v-access="{ paths: ['/users'] }" type="primary" @click="openCreate">新增账号</el-button>
      </div>

      <div class="page-toolbar">
        <el-input v-model="keyword" placeholder="搜索账号 / 姓名 / 邮箱" clearable @keyup.enter="loadData" />
        <el-tree-select
          v-model="selectedOrgId"
          :data="orgTreeOptions"
          check-strictly
          clearable
          node-key="id"
          :props="{ label: 'orgName', children: 'children' }"
          placeholder="按组织筛选"
        />
        <el-button type="primary" class="query-btn" @click="loadData">查询</el-button>
      </div>

      <div class="table-shell">
        <PageLoadingOverlay :loading="pageLoading" compact>
          <el-table :data="records" border>
            <el-table-column prop="username" label="账号" min-width="140" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column prop="email" label="邮箱" min-width="200" />
            <el-table-column prop="orgName" label="组织" min-width="180">
              <template #default="{ row }">
                {{ row.orgName || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="200">
              <template #default="{ row }">
                <el-tag v-for="name in row.roleNames" :key="name" class="tag-gap">{{ name }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button v-access="{ paths: ['/users'] }" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button v-access="{ paths: ['/users'] }" link type="warning" @click="openResetPassword(row)">重置密码</el-button>
                <el-button v-access="{ paths: ['/users'] }" link type="danger" @click="removeUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </PageLoadingOverlay>
      </div>

      <div class="page-pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="page"
          :page-size="size"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑账号' : '新增账号'"
      width="720px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <DialogHero
          :title="editingId ? '编辑账号信息' : '创建新账号'"
          :description="editingId ? '调整账号资料、角色与启用状态。' : '配置账号、初始密码与角色权限。'"
        />
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <div class="dialog-section">
          <div class="section-title">基础资料</div>
          <div class="form-grid">
            <el-form-item label="账号" prop="username">
              <el-input v-model="form.username" :disabled="Boolean(editingId)" placeholder="例如：admin.user" />
              <div class="field-tip">账号不允许中文，需以字母开头，可包含数字、点、下划线和中划线。</div>
            </el-form-item>

            <el-form-item v-if="!editingId" label="初始密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="8-20 位，至少包含字母和数字" />
              <div class="field-tip">建议使用大小写字母、数字和常见符号组合。</div>
            </el-form-item>

            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="所属组织" prop="orgId">
              <el-tree-select
                v-model="form.orgId"
                :data="orgTreeOptions"
                check-strictly
                clearable
                node-key="id"
                :props="{ label: 'orgName', children: 'children' }"
                style="width: 100%"
                placeholder="请选择所属组织"
              />
            </el-form-item>

            <el-form-item label="头像" prop="avatar" class="span-2">
              <ImageUploadField
                v-model="form.avatar"
                key-prefix="avatars"
              />
            </el-form-item>
          </div>
        </div>

        <div class="dialog-section">
          <div class="section-title">权限与状态</div>
          <div class="form-grid">
            <el-form-item label="角色" prop="roleIds" class="span-2">
              <el-select v-model="form.roleIds" multiple style="width: 100%" placeholder="请选择角色">
                <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="启用状态">
              <div class="switch-box">
                <el-switch v-model="enabled" inline-prompt active-text="启" inactive-text="停" />
                <span class="switch-label">{{ enabled ? '当前启用' : '当前禁用' }}</span>
              </div>
            </el-form-item>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="resetDialogVisible"
      title="重置密码"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <DialogHero
          title="重置账号密码"
          :description="`为账号 ${resetTarget?.username || ''} 设置一个新的登录密码。`"
        />
      </template>

      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-position="top" class="dialog-form">
        <div class="dialog-section">
          <div class="section-title">密码设置</div>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="8-20 位，至少包含字母和数字" />
            <div class="field-tip">重置后将立即生效，建议通知该账号持有人尽快登录并修改。</div>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { managementApi, type OrgItem, type RoleItem, type UserManageItem } from '@/api'
import DialogHero from '@/components/DialogHero.vue'
import ImageUploadField from '@/components/ImageUploadField.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'

const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pageLoading = ref(false)
const records = ref<UserManageItem[]>([])
const roleOptions = ref<RoleItem[]>([])
const orgOptions = ref<OrgItem[]>([])
const dialogVisible = ref(false)
const resetDialogVisible = ref(false)
const editingId = ref<number | null>(null)
const resetTarget = ref<UserManageItem | null>(null)
const formRef = ref<FormInstance>()
const resetFormRef = ref<FormInstance>()
const selectedOrgId = ref<number | undefined>()
const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  avatar: '',
  orgId: undefined as number | undefined,
  roleIds: [] as number[],
  status: 1,
})
const resetForm = reactive({
  newPassword: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度需为 4-20 位', trigger: 'blur' },
    {
      pattern: /^[A-Za-z][A-Za-z0-9._-]*$/,
      message: '账号需以字母开头，且不能包含中文',
      trigger: 'blur',
    },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (editingId.value) return callback()
        if (!value) return callback(new Error('请输入初始密码'))
        if (value.length < 8 || value.length > 20) return callback(new Error('密码长度需为 8-20 位'))
        if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
          return callback(new Error('密码至少包含字母和数字'))
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度需为 2-50 位', trigger: 'blur' },
  ],
  email: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback()
        const ok = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/.test(value)
        callback(ok ? undefined : new Error('邮箱格式不正确'))
      },
      trigger: 'blur',
    },
  ],
  roleIds: [
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一个角色'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

const resetRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度需为 8-20 位', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@#$%^&+=!?._-]+$/,
      message: '密码至少包含字母和数字',
      trigger: 'blur',
    },
  ],
}

const enabled = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  },
})

const orgTreeOptions = computed(() => orgOptions.value.map(markOrgLabel))

async function loadRoles() {
  const res = await managementApi.listRoles() as any
  roleOptions.value = res.data || []
}

async function loadOrgs() {
  const res = await managementApi.orgOptions() as any
  orgOptions.value = res.data || []
}

async function loadData() {
  pageLoading.value = true
  try {
    const res = await managementApi.listUsers({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      orgId: selectedOrgId.value,
    }) as any
    total.value = res.data?.total || 0
    records.value = res.data?.records || []
  } finally {
    pageLoading.value = false
  }
}

function resetFormData() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.avatar = ''
  form.orgId = undefined
  form.roleIds = []
  form.status = 1
}

function openCreate() {
  resetFormData()
  const defaultRole = roleOptions.value.find(role => role.roleCode === 'USER')
  form.roleIds = defaultRole ? [defaultRole.id] : []
  dialogVisible.value = true
}

function openEdit(row: UserManageItem) {
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  form.realName = row.realName || ''
  form.email = row.email || ''
  form.avatar = row.avatar || ''
  form.orgId = row.orgId
  form.roleIds = [...(row.roleIds || [])]
  form.status = row.status
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate()
  if (editingId.value) {
    await managementApi.updateUser(editingId.value, {
      realName: form.realName,
      email: form.email,
      avatar: form.avatar,
      orgId: form.orgId,
      roleIds: form.roleIds,
      status: form.status,
    })
    ElMessage.success('账号已更新')
  } else {
    await managementApi.createUser({
      username: form.username,
      password: form.password,
      realName: form.realName,
      email: form.email,
      avatar: form.avatar,
      orgId: form.orgId,
      roleIds: form.roleIds,
      status: form.status,
    })
    ElMessage.success('账号已创建')
  }
  dialogVisible.value = false
  await loadData()
}

function openResetPassword(row: UserManageItem) {
  resetTarget.value = row
  resetForm.newPassword = ''
  resetDialogVisible.value = true
}

async function submitResetPassword() {
  if (!resetTarget.value) return
  await resetFormRef.value?.validate()
  await managementApi.resetUserPassword(resetTarget.value.id, resetForm.newPassword)
  ElMessage.success('密码已重置')
  resetDialogVisible.value = false
}

async function removeUser(row: UserManageItem) {
  await ElMessageBox.confirm(`确认删除账号 ${row.username} 吗？`, '提示', { type: 'warning' })
  await managementApi.deleteUser(row.id)
  ElMessage.success('账号已删除')
  await loadData()
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  loadData()
}

function markOrgLabel(item: OrgItem): OrgItem {
  return {
    ...item,
    orgName: item.status === 1 ? item.orgName : `${item.orgName}（禁用）`,
    children: (item.children || []).map(markOrgLabel),
  }
}

onMounted(async () => {
  pageLoading.value = true
  try {
    await Promise.all([loadRoles(), loadOrgs()])
    await loadData()
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped>
.manage-page :deep(.el-card__body) {
  position: relative;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr) auto;
  min-height: calc(100vh - 228px);
}

.page-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 18px;
}

.tag-gap {
  margin-right: 6px;
  margin-bottom: 4px;
}

.page-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 320px) minmax(0, 260px) auto;
  gap: 12px;
  align-items: center;
  margin-bottom: 18px;
}

.page-toolbar :deep(.el-input),
.page-toolbar :deep(.el-tree-select) {
  width: 100%;
}

.query-btn {
  min-width: 96px;
  justify-self: start;
}

.page-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.table-shell {
  border-radius: 18px;
  min-height: 0;
}

.table-shell :deep(.page-loading-shell),
.table-shell :deep(.el-table) {
  min-height: 100%;
}

.switch-box {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.switch-label {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

@media (max-width: 960px) {
  .page-toolbar {
    grid-template-columns: 1fr;
  }

  .query-btn {
    width: 100%;
  }
}
</style>
