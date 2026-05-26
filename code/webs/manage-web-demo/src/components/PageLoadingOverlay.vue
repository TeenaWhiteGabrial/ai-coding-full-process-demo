<template>
  <div class="page-loading-shell" :class="{ loading }">
    <slot />

    <transition name="page-loading-fade">
      <div v-if="loading" class="page-loading-mask" aria-live="polite" aria-busy="true">
        <div class="page-loading-skeleton" :class="{ compact }">
          <div class="skeleton-badge"></div>
          <div class="skeleton-title"></div>
          <div class="skeleton-copy"></div>
          <div class="skeleton-copy short"></div>
          <div class="skeleton-grid">
            <span v-for="item in compact ? 3 : 6" :key="item" class="skeleton-card"></span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  compact?: boolean
  loading: boolean
}>()
</script>

<style scoped>
.page-loading-shell {
  position: relative;
}

.page-loading-shell.loading {
  min-height: 200px;
}

.page-loading-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  padding: 14px;
  border-radius: inherit;
  background:
    linear-gradient(180deg, hsl(var(--card) / 0.88), hsl(var(--card) / 0.94));
  backdrop-filter: blur(10px);
  z-index: 10;
}

.page-loading-skeleton {
  width: 100%;
  display: grid;
  align-content: start;
  gap: 14px;
  padding: 12px;
}

.page-loading-skeleton.compact {
  gap: 10px;
  padding: 4px;
}

.skeleton-badge,
.skeleton-title,
.skeleton-copy,
.skeleton-card {
  position: relative;
  overflow: hidden;
  border-radius: 999px;
  background: hsl(var(--secondary));
}

.skeleton-badge::after,
.skeleton-title::after,
.skeleton-copy::after,
.skeleton-card::after {
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, hsl(var(--card) / 0.72), transparent);
  animation: page-loading-shimmer 1.2s ease-in-out infinite;
  content: '';
}

.skeleton-badge {
  width: 108px;
  height: 14px;
}

.skeleton-title {
  width: min(320px, 56%);
  height: 20px;
  border-radius: 12px;
}

.skeleton-copy {
  width: min(520px, 88%);
  height: 12px;
}

.skeleton-copy.short {
  width: min(360px, 64%);
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 4px;
}

.skeleton-card {
  height: 104px;
  border-radius: 22px;
  background:
    linear-gradient(180deg, hsl(var(--panel) / 0.96), hsl(var(--secondary) / 0.95));
  border: 1px solid hsl(var(--border) / 0.85);
}

.page-loading-fade-enter-active,
.page-loading-fade-leave-active {
  transition: opacity 0.2s ease;
}

.page-loading-fade-enter-from,
.page-loading-fade-leave-to {
  opacity: 0;
}

@keyframes page-loading-shimmer {
  100% {
    transform: translateX(100%);
  }
}
</style>
