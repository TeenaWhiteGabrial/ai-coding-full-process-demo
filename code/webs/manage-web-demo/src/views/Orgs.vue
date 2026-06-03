<template>
  <div class="manage-page">
    <el-card>
      <div class="page-actions">
        <el-button v-access="{ paths: ['/orgs'] }" type="primary" @click="openCreateRoot">新增顶级组织</el-button>
      </div>

      <div class="table-shell">
        <PageLoadingOverlay :loading="pageLoading" compact>
          <el-table :data="orgs" border row-key="id" default-expand-all style="width: 100%">
            <el-table-column prop="orgName" label="组织名称" min-width="180" />
            <el-table-column prop="orgCode" label="组织编码" min-width="140" />
            <el-table-column prop="leaderName" label="负责人" min-width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button v-access="{ paths: ['/orgs'] }" link type="primary" @click="openCreateChild(row)">新增子级</el-button>
                <el-button v-access="{ paths: ['/orgs'] }" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button v-access="{ paths: ['/orgs'] }" link type="danger" @click="removeOrg(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </PageLoadingOverlay>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑组织' : '新增组织'"
      width="640px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <DialogHero
          :title="editingId ? '编辑组织信息' : '创建组织节点'"
          :description="editingId ? '维护组织层级、编码和负责人。' : '新增顶级组织或当前节点的子组织。'"
        />
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <div class="dialog-section">
          <div class="section-title">组织信息</div>
          <div class="form-grid">
            <el-form-item label="上级组织" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="orgOptions"
                check-strictly
                node-key="id"
                :props="{ label: 'orgName', children: 'children' }"
                style="width: 100%"
                clearable
              />
            </el-form-item>
            <el-form-item label="组织名称" prop="orgName">
              <el-input v-model="form.orgName" placeholder="请输入组织名称" />
            </el-form-item>
            <el-form-item label="组织编码" prop="orgCode">
              <el-input v-model="form.orgCode" placeholder="例如：OPS_CENTER" />
            </el-form-item>
            <el-form-item label="负责人" prop="leaderName">
              <el-input v-model="form.leaderName" placeholder="请输入负责人" />
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { managementApi, type OrgItem } from '@/api'
import DialogHero from '@/components/DialogHero.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'

const orgs = ref<OrgItem[]>([])
const orgOptions = ref<OrgItem[]>([])
const pageLoading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  parentId: 0,
  orgName: '',
  orgCode: '',
  leaderName: '',
  status: 1,
})

const rules: FormRules = {
  parentId: [{ required: true, message: '请选择上级组织', trigger: 'change' }],
  orgName: [
    { required: true, message: '请输入组织名称', trigger: 'blur' },
    { max: 100, message: '组织名称不能超过 100 个字符', trigger: 'blur' },
  ],
  orgCode: [
    { required: true, message: '请输入组织编码', trigger: 'blur' },
    { max: 50, message: '组织编码不能超过 50 个字符', trigger: 'blur' },
  ],
  leaderName: [{ max: 50, message: '负责人不能超过 50 个字符', trigger: 'blur' }],
}

const enabled = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  },
})

function normalizeOrgOptions(items: OrgItem[], currentId?: number | null): OrgItem[] {
  return items
    .filter(item => item.id !== currentId)
    .map(item => ({
      ...item,
      orgName: item.status === 1 ? item.orgName : `${item.orgName}（禁用）`,
      children: normalizeOrgOptions(item.children || [], currentId),
    }))
}

async function loadData() {
  pageLoading.value = true
  try {
    const [treeRes, optionRes] = await Promise.all([
      managementApi.orgTree() as any,
      managementApi.orgOptions() as any,
    ])
    orgs.value = treeRes.data || []
    orgOptions.value = [{ id: 0, parentId: 0, orgName: '顶级组织', orgCode: '', status: 1, children: optionRes.data || [] }]
  } finally {
    pageLoading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.parentId = 0
  form.orgName = ''
  form.orgCode = ''
  form.leaderName = ''
  form.status = 1
}

function openCreateRoot() {
  resetForm()
  dialogVisible.value = true
}

function openCreateChild(row: OrgItem) {
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}

function openEdit(row: OrgItem) {
  editingId.value = row.id
  form.parentId = row.parentId || 0
  form.orgName = row.orgName
  form.orgCode = row.orgCode
  form.leaderName = row.leaderName || ''
  form.status = row.status
  orgOptions.value = [{ id: 0, parentId: 0, orgName: '顶级组织', orgCode: '', status: 1, children: normalizeOrgOptions(orgs.value, row.id) }]
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate()
  const payload = {
    parentId: form.parentId,
    orgName: form.orgName,
    orgCode: form.orgCode,
    leaderName: form.leaderName,
    status: form.status,
  }
  if (editingId.value) {
    await managementApi.updateOrg(editingId.value, payload)
    ElMessage.success('组织已更新')
  } else {
    await managementApi.createOrg(payload)
    ElMessage.success('组织已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function removeOrg(row: OrgItem) {
  await ElMessageBox.confirm(`确认删除组织 ${row.orgName} 吗？`, '提示', { type: 'warning' })
  await managementApi.deleteOrg(row.id)
  ElMessage.success('组织已删除')
  await loadData()
}

onMounted(loadData)
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

.switch-box {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.switch-label {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}
</style>
