<template>
  <div class="tree-branch">
    <div class="tree-node" :class="nodeClass">
      <div class="tree-node-main">
        <div class="tree-node-title">
          <strong>{{ node.dictLabel }}</strong>
          <el-tag size="small" effect="plain">{{ node.dictValue || '-' }}</el-tag>
        </div>
        <div class="tree-node-meta">
          <span>排序：{{ node.sort ?? 0 }}</span>
          <span>备注：{{ node.remark || '-' }}</span>
        </div>
      </div>

      <div class="tree-node-actions">
        <el-tag size="small" :type="node.status === 1 ? 'success' : 'info'">
          {{ node.status === 1 ? '启用' : '禁用' }}
        </el-tag>
        <el-button v-access="{ permissions: ['dict:create'] }" link type="primary" @click="$emit('create-child', node)">
          新增子级
        </el-button>
        <el-button v-access="{ permissions: ['dict:update'] }" link type="primary" @click="$emit('edit', node)">
          编辑
        </el-button>
        <el-button v-access="{ permissions: ['dict:delete'] }" link type="danger" @click="$emit('remove', node)">
          删除
        </el-button>
      </div>
    </div>

    <div v-if="node.children?.length" class="tree-children">
      <DictTreeBranch
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :level="level + 1"
        @create-child="$emit('create-child', $event)"
        @edit="$emit('edit', $event)"
        @remove="$emit('remove', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import type { DictItem } from '@/api/management'

defineOptions({
  name: 'DictTreeBranch',
})

const props = defineProps<{
  node: DictItem
  level?: number
}>()

defineEmits<{
  'create-child': [node: DictItem]
  edit: [node: DictItem]
  remove: [node: DictItem]
}>()

const nodeClass = computed(() => {
  if ((props.level || 0) <= 0) return ''
  if ((props.level || 0) === 1) return 'tree-node-child'
  return 'tree-node-grandchild'
})
</script>

<style scoped>
.tree-branch {
  display: grid;
  gap: 12px;
}

.tree-children {
  display: grid;
  gap: 12px;
  margin-left: 26px;
  padding-left: 18px;
  border-left: 1px dashed hsl(var(--border));
}

.tree-node {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid hsl(var(--border));
  border-radius: 16px;
  background: hsl(var(--card));
}

.tree-node-child {
  background: hsl(var(--panel));
}

.tree-node-grandchild {
  background: hsl(var(--card) / 0.92);
}

.tree-node-main {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.tree-node-title,
.tree-node-meta,
.tree-node-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tree-node-meta {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.tree-node-actions {
  align-items: center;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .tree-node {
    flex-direction: column;
  }

  .tree-node-actions {
    justify-content: flex-start;
  }
}
</style>
