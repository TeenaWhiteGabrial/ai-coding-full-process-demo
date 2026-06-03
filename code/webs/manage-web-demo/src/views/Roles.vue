<template>
  <div class="manage-page">
    <el-card>
      <div class="page-actions">
        <el-button v-access="{ paths: ['/roles'] }" type="primary" @click="openCreate">新建角色</el-button>
      </div>

      <div class="table-shell">
        <PageLoadingOverlay :loading="pageLoading" compact>
          <el-table :data="roles" border style="width: 100%">
            <el-table-column prop="roleCode" label="角色编码" min-width="160" />
            <el-table-column prop="roleName" label="角色名称" min-width="160" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button v-access="{ paths: ['/roles'] }" link type="primary" :disabled="row.roleCode === 'SUPER_ADMIN'" @click="openMenus(row)">
                  菜单权限
                </el-button>
                <el-button v-access="{ paths: ['/roles'] }" link type="danger" :disabled="row.roleCode === 'SUPER_ADMIN'" @click="removeRole(row)">
                  删除角色
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </PageLoadingOverlay>
      </div>
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
        <DialogHero title="创建角色" description="定义新的角色编码和展示名称，后续可继续分配菜单权限。" />
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
        <DialogHero
          title="角色菜单权限"
          :description="`为角色 ${currentRole?.roleName || ''} 选择可访问的菜单。`"
        />
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { managementApi, type RoleItem, type RoleMenuTreeItem } from '@/api/management'
import DialogHero from '@/components/DialogHero.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'

const roles = ref<RoleItem[]>([])
const pageLoading = ref(false)
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
  pageLoading.value = true
  try {
    const res = await managementApi.roleList() as any
    roles.value = res.data || []
  } finally {
    pageLoading.value = false
  }
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

async function removeRole(role: RoleItem) {
  await ElMessageBox.confirm(`确认删除角色 ${role.roleName} 吗？`, '提示', { type: 'warning' })
  await managementApi.deleteRole(role.id)
  ElMessage.success('角色已删除')
  await loadRoles()
}

onMounted(loadRoles)
</script>

<style scoped>
.manage-page :deep(.el-card__body) {
  position: relative;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: calc(100vh - 228px);
}

.page-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 18px;
}

.field-tip {
  margin-top: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}
</style>
