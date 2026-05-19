/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TYPE: 'console'
  readonly VITE_BASE_ROUTER: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
