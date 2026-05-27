import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { getSiteConfig, type SiteConfig } from '@/api/site'

const defaultSiteConfig: SiteConfig = {
  siteName: 'AI Studio',
  siteDescription: '',
  logoUrl: '',
  iconUrl: '',
  footerText: '',
  footerCopyright: '',
  footerRecord: '',
}

function resolveAssetUrl(url?: string, fallback = '') {
  if (!url) return fallback
  if (/^(https?:)?\/\//.test(url)) return url
  if (url.startsWith('/uploads/')) return url
  return `${import.meta.env.BASE_URL}${url.replace(/^\//, '')}`
}

function ensureFavicon() {
  let favicon = document.querySelector("link[rel='icon']") as HTMLLinkElement | null
  if (!favicon) {
    favicon = document.createElement('link')
    favicon.rel = 'icon'
    document.head.appendChild(favicon)
  }
  return favicon
}

export const useSiteStore = defineStore('site', () => {
  const config = ref<SiteConfig>({ ...defaultSiteConfig })
  const loaded = ref(false)
  const loading = ref(false)

  const resolvedLogoUrl = computed(() =>
    resolveAssetUrl(config.value.logoUrl, `${import.meta.env.BASE_URL}ai-studio-logo.svg`),
  )
  const resolvedIconUrl = computed(() => resolveAssetUrl(config.value.iconUrl))

  function applySiteConfig(nextConfig: SiteConfig) {
    config.value = { ...defaultSiteConfig, ...nextConfig }
    document.title = config.value.siteName || defaultSiteConfig.siteName

    const favicon = ensureFavicon()
    favicon.href = resolvedIconUrl.value || `${import.meta.env.BASE_URL}favicon.ico`
  }

  async function fetchSiteConfig(force = false) {
    if (loading.value) return config.value
    if (loaded.value && !force) return config.value

    loading.value = true
    try {
      const nextConfig = await getSiteConfig()
      applySiteConfig(nextConfig)
      loaded.value = true
      return config.value
    } finally {
      loading.value = false
    }
  }

  return {
    applySiteConfig,
    config,
    fetchSiteConfig,
    loaded,
    loading,
    resolvedIconUrl,
    resolvedLogoUrl,
  }
})
