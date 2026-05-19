import request from '@/utils/request'

export interface UploadResult {
  fileName: string
  ossKey: string
  ossUrl: string
  fileSize: number
}

export const uploadApi = {
  upload: async (file: File, keyPrefix = 'uploads') => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await request.post(`/common/oss/upload?keyPrefix=${encodeURIComponent(keyPrefix)}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    const payload = response as any
    const item = payload.data || {}
    payload.data = {
      fileName: item.file_name ?? item.fileName ?? '',
      ossKey: item.oss_key ?? item.ossKey ?? '',
      ossUrl: item.oss_url ?? item.ossUrl ?? '',
      fileSize: item.file_size ?? item.fileSize ?? 0,
    } as UploadResult
    return payload
  },
}
