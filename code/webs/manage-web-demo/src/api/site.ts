import request from '@/utils/request'

export interface SiteConfig {
  siteName: string
  siteDescription: string
  logoUrl: string
  iconUrl: string
  footerText: string
  footerCopyright: string
  footerRecord: string
}

export async function getSiteConfig() {
  const response = await request.get('/common/site/config') as any
  return response.data as SiteConfig
}

export async function getSiteConfigForManagement() {
  const response = await request.get('/common/site') as any
  return response.data as SiteConfig
}

export async function updateSiteConfig(data: SiteConfig) {
  return request.put('/common/site', data)
}
