<template>
  <div class="dict-page">
    <el-card class="dict-layout-card">
      <div class="dict-layout">
        <aside class="dict-sidebar">
          <div class="sidebar-head">
            <div>
              <h3>字典列表</h3>
            </div>
            <el-button v-access="{ permissions: ['dict:create'] }" type="primary" @click="openCreateRoot">
              新增一级字典
            </el-button>
          </div>

          <PageLoadingOverlay :loading="pageLoading" compact>
            <div class="dict-nav">
              <button
                v-for="item in rootRecords"
                :key="item.id"
                type="button"
                class="dict-nav-item"
                :class="{ active: item.id === activeRootId }"
                @click="activeRootId = item.id"
              >
                <div>
                  <strong>{{ item.dictLabel }}</strong>
                  <span>{{ item.dictType || '未设置类型' }}</span>
                </div>
                <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
                  {{ item.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </button>

              <el-empty v-if="!rootRecords.length && !pageLoading" description="暂无一级字典" />
            </div>
          </PageLoadingOverlay>
        </aside>

        <section class="dict-content">
          <template v-if="currentRoot">
            <div class="content-head">
              <div>
                <h2>{{ currentRoot.dictLabel }}</h2>
                <p>
                  类型：{{ currentRoot.dictType || '-' }}
                  <span class="content-dot">·</span>
                  值：{{ currentRoot.dictValue || '-' }}
                </p>
              </div>

              <div class="content-actions">
                <el-button v-access="{ permissions: ['dict:create'] }" type="primary" @click="openCreateChild(currentRoot)">
                  新增子级
                </el-button>
                <el-button v-access="{ permissions: ['dict:update'] }" @click="openEdit(currentRoot)">
                  编辑当前字典
                </el-button>
                <el-button v-access="{ permissions: ['dict:delete'] }" type="danger" plain @click="removeItem(currentRoot)">
                  删除当前字典
                </el-button>
              </div>
            </div>

            <el-card class="summary-card" shadow="never">
              <div class="summary-grid">
                <div class="summary-item">
                  <span>状态</span>
                  <strong>{{ currentRoot.status === 1 ? '启用' : '禁用' }}</strong>
                </div>
                <div class="summary-item">
                  <span>排序</span>
                  <strong>{{ currentRoot.sort ?? 0 }}</strong>
                </div>
                <div class="summary-item">
                  <span>子级数量</span>
                  <strong>{{ descendantCount }}</strong>
                </div>
                <div class="summary-item">
                  <span>备注</span>
                  <strong>{{ currentRoot.remark || '-' }}</strong>
                </div>
              </div>
            </el-card>

            <el-card class="tree-card" shadow="never">
              <template #header>
                <div class="tree-head">
                  <div>
                    <h3>全部子级内容</h3>
                  </div>
                  <span class="tree-count">{{ descendantCount }} 项</span>
                </div>
              </template>

              <div class="table-shell">
                <PageLoadingOverlay :loading="pageLoading" compact>
                  <el-table v-if="currentRoot.children?.length" :data="currentRoot.children" border row-key="id" default-expand-all style="width: 100%">
                    <el-table-column prop="dictLabel" label="名称" min-width="180" />
                    <el-table-column prop="dictValue" label="值" min-width="180" />
                    <el-table-column prop="dictType" label="类型" min-width="140" />
                    <el-table-column prop="sort" label="排序" width="90" />
                    <el-table-column label="状态" width="100">
                      <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">
                          {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="remark" label="备注" min-width="220" />
                    <el-table-column label="操作" width="220" fixed="right">
                      <template #default="{ row }">
                        <el-button v-access="{ permissions: ['dict:create'] }" link type="primary" @click="openCreateChild(row)">
                          新增子级
                        </el-button>
                        <el-button v-access="{ permissions: ['dict:update'] }" link type="primary" @click="openEdit(row)">
                          编辑
                        </el-button>
                        <el-button v-access="{ permissions: ['dict:delete'] }" link type="danger" @click="removeItem(row)">
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-empty v-else description="当前字典下暂无子级数据" />
                </PageLoadingOverlay>
              </div>
            </el-card>
          </template>

          <el-empty v-else description="请选择左侧字典查看内容" />
        </section>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑字典项' : '新增字典项'"
      width="680px"
      destroy-on-close
      :close-on-click-modal="false"
      class="manage-editor-dialog"
    >
      <template #header>
        <DialogHero
          :title="editingId ? '编辑字典配置' : '创建字典项'"
          description="支持一级和二级字典结构，可按业务类型维护目录与子项。"
        />
      </template>

      <el-form :model="form" label-position="top" class="dialog-form">
        <div class="dialog-section">
          <div class="section-title">字典信息</div>
          <div class="form-grid">
            <el-form-item label="父级">
              <el-select v-model="form.parentId" clearable style="width: 100%">
                <el-option :value="0" label="顶级字典" />
                <el-option v-for="item in flatRecords" :key="item.id" :label="item.dictLabel" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="字典类型">
              <el-input v-model="form.dictType" placeholder="例如：industry" />
            </el-form-item>
            <el-form-item label="字典名称">
              <el-input v-model="form.dictLabel" placeholder="例如：船舶" />
            </el-form-item>
            <el-form-item label="字典值">
              <el-input v-model="form.dictValue" placeholder="例如：ship" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-switch v-model="enabled" inline-prompt active-text="启" inactive-text="停" />
            </el-form-item>
            <el-form-item label="备注" class="span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
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

import { managementApi, type DictItem } from '@/api/management'
import DialogHero from '@/components/DialogHero.vue'
import PageLoadingOverlay from '@/components/PageLoadingOverlay.vue'

const pageLoading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const records = ref<DictItem[]>([])
const activeRootId = ref<number | null>(null)
const form = reactive({
  parentId: 0,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  status: 1,
  sort: 0,
  remark: '',
})

const enabled = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  },
})

const rootRecords = computed(() => records.value.filter(item => (item.parentId || 0) === 0))
const currentRoot = computed(() => rootRecords.value.find(item => item.id === activeRootId.value) || null)
const currentTreeRows = computed(() => currentRoot.value?.children || [])
const descendantCount = computed(() => countDescendants(currentTreeRows.value))

const flatRecords = computed(() => {
  const items: DictItem[] = []
  const walk = (nodes: DictItem[]) => {
    for (const node of nodes) {
      items.push(node)
      if (node.children?.length) walk(node.children)
    }
  }
  walk(records.value)
  return items
})

function syncActiveRoot() {
  if (!rootRecords.value.length) {
    activeRootId.value = null
    return
  }
  if (!rootRecords.value.some(item => item.id === activeRootId.value)) {
    activeRootId.value = rootRecords.value[0].id
  }
}

async function loadData() {
  pageLoading.value = true
  try {
    const res = await managementApi.dictTree() as any
    records.value = res.data || []
    syncActiveRoot()
  } finally {
    pageLoading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.parentId = 0
  form.dictType = currentRoot.value?.dictType || ''
  form.dictLabel = ''
  form.dictValue = ''
  form.status = 1
  form.sort = 0
  form.remark = ''
}

function openCreateRoot() {
  resetForm()
  form.dictType = ''
  dialogVisible.value = true
}

function openCreateChild(row: DictItem) {
  resetForm()
  form.parentId = row.id
  form.dictType = row.dictType
  dialogVisible.value = true
}

function openEdit(row: DictItem) {
  editingId.value = row.id
  form.parentId = row.parentId || 0
  form.dictType = row.dictType
  form.dictLabel = row.dictLabel
  form.dictValue = row.dictValue
  form.status = row.status
  form.sort = row.sort || 0
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function submit() {
  const payload = {
    parentId: form.parentId,
    dictType: form.dictType,
    dictLabel: form.dictLabel,
    dictValue: form.dictValue,
    status: form.status,
    sort: form.sort,
    remark: form.remark,
  }
  if (editingId.value) {
    await managementApi.updateDict(editingId.value, payload)
    ElMessage.success('字典项已更新')
  } else {
    await managementApi.createDict(payload)
    ElMessage.success('字典项已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function removeItem(row: DictItem) {
  await ElMessageBox.confirm(`确认删除字典项 ${row.dictLabel} 吗？`, '提示', { type: 'warning' })
  await managementApi.deleteDict(row.id)
  ElMessage.success('字典项已删除')
  await loadData()
}

function countDescendants(items: DictItem[]): number {
  return items.reduce((total, item) => total + 1 + countDescendants(item.children || []), 0)
}

onMounted(loadData)
</script>

<style scoped>
.dict-layout-card :deep(.el-card__body) {
  position: relative;
  padding: 20px;
  min-height: calc(100vh - 228px);
}

.dict-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
  min-height: calc(100vh - 268px);
}

.dict-sidebar,
.dict-content {
  min-height: 0;
}

.dict-sidebar {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 14px;
  padding: 16px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card));
}

.sidebar-head,
.content-head,
.tree-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sidebar-head h3,
.content-head h2,
.tree-head h3 {
  margin: 0;
  color: hsl(var(--foreground));
}

.dict-nav {
  display: grid;
  gap: 8px;
  align-content: start;
}

.dict-nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card));
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.dict-nav-item strong,
.summary-item strong {
  display: block;
  color: hsl(var(--foreground));
}

.dict-nav-item span,
.content-head p,
.summary-item span {
  color: hsl(var(--muted-foreground));
}

.dict-nav-item.active {
  background: var(--theme-primary-subtle);
  border-color: hsl(var(--primary) / 0.2);
  box-shadow: none;
  transform: none;
}

.dict-content {
  display: grid;
  align-content: start;
  gap: 12px;
}

.content-head {
  padding: 14px 16px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card));
}

.content-head p {
  margin: 4px 0 0;
}

.content-dot {
  margin: 0 8px;
}

.content-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.summary-card,
.tree-card {
  border-radius: 8px;
}

.summary-card :deep(.el-card__body),
.tree-card :deep(.el-card__body) {
  padding: 12px;
}

.tree-card :deep(.el-card__header) {
  padding: 12px 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-item {
  padding: 8px 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--panel));
}

.summary-item span {
  display: block;
  margin-bottom: 2px;
  font-size: 12px;
}

.summary-item strong {
  line-height: 1.15;
  font-size: 15px;
}

.tree-count {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

@media (max-width: 1100px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .content-head,
  .sidebar-head,
  .tree-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .content-actions {
    justify-content: flex-start;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
