<template>
  <div class="manage-page">
    <el-card>
      <template #header>
        <div class="page-head">
          <div>
            <span class="kicker">菜单管理</span>
            <h2>控制台菜单</h2>
          </div>
          <el-button type="primary" @click="openCreate">新增菜单</el-button>
        </div>
      </template>

      <el-table :data="menus" border row-key="id" default-expand-all>
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="path" label="路径" min-width="180" />
        <el-table-column prop="component" label="组件" min-width="160" />
        <el-table-column label="图标" width="160">
          <template #default="{ row }">
            <div class="icon-preview-cell">
              <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
              <span>{{ row.icon || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="隐藏" width="90">
          <template #default="{ row }">
            {{ row.hidden === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeMenu(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑菜单' : '新增菜单'"
      width="640px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <div class="dialog-hero">
          <div class="dialog-title">{{ editingId ? '编辑菜单配置' : '创建菜单项' }}</div>
          <div class="dialog-subtitle">维护控制台菜单的名称、路径、排序与显隐状态。</div>
        </div>
      </template>

      <el-form :model="form" label-position="top" class="dialog-form">
        <div class="dialog-section">
          <div class="section-title">菜单信息</div>
          <div class="form-grid">
            <el-form-item label="名称">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="父级菜单">
              <el-select v-model="form.parentId" clearable style="width: 100%">
                <el-option :value="0" label="顶级菜单" />
                <el-option v-for="item in flatMenus" :key="item.id" :value="item.id" :label="item.name" />
              </el-select>
            </el-form-item>
            <el-form-item label="路径">
              <el-input v-model="form.path" />
            </el-form-item>
            <el-form-item label="组件">
              <el-input v-model="form.component" />
            </el-form-item>
            <el-form-item label="图标">
              <el-select v-model="form.icon" clearable style="width: 100%" placeholder="请选择图标">
                <el-option v-for="icon in iconOptions" :key="icon" :label="icon" :value="icon">
                  <div class="icon-option">
                    <el-icon><component :is="icon" /></el-icon>
                    <span>{{ icon }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
            <el-form-item label="隐藏">
              <el-switch v-model="hiddenSwitch" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { managementApi, type MenuItem } from '@/api/management'

interface MenuTreeItem extends MenuItem {
  children?: MenuTreeItem[]
}

const menus = ref<MenuTreeItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const iconOptions = ['House', 'Setting', 'User', 'Key', 'Menu', 'Tools', 'Grid', 'Folder', 'Document', 'Monitor', 'DataAnalysis']
const form = reactive({
  name: '',
  parentId: 0,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  hidden: 0,
})

const hiddenSwitch = computed({
  get: () => form.hidden === 1,
  set: (value: boolean) => {
    form.hidden = value ? 1 : 0
  },
})

const flatMenus = computed(() => {
  const result: MenuItem[] = []
  const walk = (items: MenuTreeItem[]) => {
    for (const item of items) {
      result.push(item)
      if (item.children?.length) walk(item.children)
    }
  }
  walk(menus.value)
  return result
})

function buildTree(items: MenuItem[]) {
  const map = new Map<number, MenuTreeItem>()
  const roots: MenuTreeItem[] = []
  items.forEach(item => map.set(item.id, { ...item, children: [] }))
  map.forEach(item => {
    if (item.parentId && item.parentId !== 0) {
      map.get(item.parentId)?.children?.push(item)
    } else {
      roots.push(item)
    }
  })
  return roots
}

async function loadMenus() {
  const res = await managementApi.menuList() as any
  menus.value = buildTree(res.data || [])
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.parentId = 0
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sort = 0
  form.hidden = 0
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: MenuItem) {
  editingId.value = row.id
  form.name = row.name
  form.parentId = row.parentId || 0
  form.path = row.path || ''
  form.component = row.component || ''
  form.icon = row.icon || ''
  form.sort = row.sort || 0
  form.hidden = row.hidden || 0
  dialogVisible.value = true
}

async function submit() {
  const payload = {
    name: form.name,
    parentId: form.parentId,
    path: form.path,
    component: form.component,
    icon: form.icon,
    sort: form.sort,
    hidden: form.hidden,
  }
  if (editingId.value) {
    await managementApi.updateMenu(editingId.value, payload)
    ElMessage.success('菜单已更新')
  } else {
    await managementApi.createMenu(payload)
    ElMessage.success('菜单已创建')
  }
  dialogVisible.value = false
  await loadMenus()
}

async function removeMenu(row: MenuItem) {
  await ElMessageBox.confirm(`确认删除菜单 ${row.name} 吗？`, '提示', { type: 'warning' })
  await managementApi.deleteMenu(row.id)
  ElMessage.success('菜单已删除')
  await loadMenus()
}

onMounted(loadMenus)
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

.icon-preview-cell,
.icon-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
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

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
