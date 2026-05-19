<template>
  <div class="manage-page">
    <el-card>
      <template #header>
        <div class="page-head">
          <div>
            <span class="kicker">角色管理</span>
            <h2>角色与权限</h2>
          </div>
          <el-button type="primary" @click="openCreate">新建角色</el-button>
        </div>
      </template>

      <el-table :data="roles" border>
        <el-table-column prop="roleCode" label="角色编码" min-width="160" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.roleCode === 'SUPER_ADMIN'" @click="openMenus(row)">
              菜单权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="createDialogVisible"
      title="新建角色"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <div class="dialog-hero">
          <div class="dialog-title">创建角色</div>
          <div class="dialog-subtitle">定义新的角色编码和展示名称，后续可继续分配菜单权限。</div>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <div class="dialog-section">
          <div class="section-title">角色信息</div>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="form.roleCode" placeholder="例如：CONTENT_ADMIN" />
            <div class="field-tip">使用大写字母、数字和下划线，且必须以字母开头。</div>
          </el-form-item>
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="form.roleName" placeholder="例如：内容管理员" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="`配置角色菜单：${currentRole?.roleName || ''}`"
      width="720px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <div class="dialog-hero">
          <div class="dialog-title">角色菜单权限</div>
          <div class="dialog-subtitle">为角色 {{ currentRole?.roleName || '' }} 选择可访问的菜单。</div>
        </div>
      </template>

      <div class="dialog-section">
        <div class="section-title">菜单树</div>
        <el-tree
          v-if="treeData.length"
          ref="treeRef"
          :data="treeData"
          node-key="id"
          show-checkbox
          default-expand-all
          :props="{ label: 'name', children: 'children' }"
          :default-checked-keys="checkedKeys"
        />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { managementApi, type RoleItem, type RoleMenuTreeItem } from '@/api/management'

const roles = ref<RoleItem[]>([])
const dialogVisible = ref(false)
const createDialogVisible = ref(false)
const currentRole = ref<RoleItem | null>(null)
const treeData = ref<RoleMenuTreeItem[]>([])
const checkedKeys = ref<number[]>([])
const treeRef = ref()
const formRef = ref<FormInstance>()
const form = reactive({
  roleCode: '',
  roleName: '',
})

const rules: FormRules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 32, message: '角色编码长度需为 2-32 位', trigger: 'blur' },
    {
      pattern: /^[A-Z][A-Z0-9_]*$/,
      message: '角色编码需使用大写字母、数字和下划线',
      trigger: 'blur',
    },
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 50, message: '角色名称不能超过 50 个字符', trigger: 'blur' },
  ],
}

async function loadRoles() {
  const res = await managementApi.roleList() as any
  roles.value = res.data || []
}

function collectChecked(nodes: RoleMenuTreeItem[], bucket: number[]) {
  for (const node of nodes) {
    if (node.checked) bucket.push(node.id)
    if (node.children?.length) collectChecked(node.children, bucket)
  }
}

function openCreate() {
  form.roleCode = ''
  form.roleName = ''
  createDialogVisible.value = true
}

async function submitCreate() {
  await formRef.value?.validate()
  await managementApi.createRole({
    roleCode: form.roleCode,
    roleName: form.roleName,
  })
  ElMessage.success('角色已创建')
  createDialogVisible.value = false
  await loadRoles()
}

async function openMenus(role: RoleItem) {
  currentRole.value = role
  const res = await managementApi.roleMenus(role.id) as any
  treeData.value = res.data || []
  const ids: number[] = []
  collectChecked(treeData.value, ids)
  checkedKeys.value = ids
  dialogVisible.value = true
}

async function saveMenus() {
  if (!currentRole.value) return
  const checked = treeRef.value?.getCheckedKeys?.() || []
  const halfChecked = treeRef.value?.getHalfCheckedKeys?.() || []
  const menuIds = [...new Set([...(checked as number[]), ...(halfChecked as number[])])]
  await managementApi.updateRoleMenus(currentRole.value.id, menuIds)
  ElMessage.success('角色菜单已更新')
  dialogVisible.value = false
}

onMounted(loadRoles)
</script>

<style scoped>
.manage-page {
  display: grid;
  gap: 18px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kicker {
  color: hsl(var(--primary));
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-head h2 {
  margin-top: 8px;
}

.dialog-hero {
  padding: 4px 0 2px;
}

.dialog-title {
  color: hsl(var(--foreground));
  font-size: 24px;
  font-weight: 700;
}

.dialog-subtitle {
  margin-top: 8px;
  color: hsl(var(--muted-foreground));
  line-height: 1.7;
}

.dialog-form {
  display: grid;
  gap: 16px;
}

.dialog-section {
  padding: 18px;
  background: hsl(var(--muted) / 0.55);
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
}

.section-title {
  margin-bottom: 14px;
  color: hsl(var(--foreground));
  font-size: 15px;
  font-weight: 700;
}

.field-tip {
  margin-top: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}

:deep(.manage-editor-dialog .el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.manage-editor-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 22px 24px 8px;
  border-bottom: 1px solid hsl(var(--border));
}

:deep(.manage-editor-dialog .el-dialog__body) {
  padding: 20px 24px 12px;
}

:deep(.manage-editor-dialog .el-dialog__footer) {
  padding: 0 24px 20px;
}
</style>
