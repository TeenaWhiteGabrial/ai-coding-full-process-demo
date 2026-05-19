import request from '@/utils/request'

export interface SiteConfig {
  siteName: string
  siteDescription: string
  logoUrl: string
  iconUrl: string
  footerText: string
}

export async function getSiteConfig() {
  const response = await request.get('/common/site/config') as any
  return response.data as SiteConfig
}
