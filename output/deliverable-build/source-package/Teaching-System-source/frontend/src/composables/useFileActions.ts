import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { isOfficeFile, convertXlsx } from './useOfficePreview'
import { isMarkdownFile, getMarkdownHtml } from './useMarkdownPreview'

const TEXT_PREVIEW_EXTENSIONS = [
  '.txt', '.log', '.csv', '.md', '.json', '.xml', '.yaml', '.yml',
  '.html', '.css', '.scss', '.less', '.js', '.ts', '.jsx', '.tsx', '.vue',
  '.java', '.py', '.cpp', '.c', '.h', '.hpp', '.cs', '.go', '.rs', '.rb',
  '.php', '.swift', '.kt', '.sql', '.sh', '.bat', '.ps1', '.ini', '.cfg',
  '.env', '.gitignore', '.properties', '.gradle', '.dockerfile',
]

const MEDIA_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.webp', '.gif', '.bmp', '.mp4', '.webm', '.mov', '.avi']

const TEXT_CONTENT_TYPES = ['text/', 'application/json', 'application/xml', 'application/javascript', 'application/x-sh']

export function isTextPreviewable(fileName: string, contentType?: string): boolean {
  if (contentType && TEXT_CONTENT_TYPES.some(t => contentType.startsWith(t))) return true
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  return TEXT_PREVIEW_EXTENSIONS.includes(ext)
}

function isMediaPreviewable(fileName: string): boolean {
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  return MEDIA_EXTENSIONS.includes(ext)
}

export function isPreviewable(fileName: string, contentType?: string): boolean {
  return isTextPreviewable(fileName, contentType) || isMediaPreviewable(fileName) || isOfficeFile(fileName)
}

export function useFileActions() {
  const router = useRouter()
  const previewVisible = ref(false)
  const previewContent = ref('')
  const previewFileName = ref('')
  const previewLoading = ref(false)
  const previewError = ref('')
  const previewMode = ref<'text' | 'image' | 'video' | 'office' | 'markdown'>('text')
  const currentFileId = ref<number | undefined>(undefined)
  const officeBuffer = ref<ArrayBuffer | null>(null)

  // ── File content cache (session-scoped, 5 min TTL) ──
  const CACHE_TTL = 5 * 60 * 1000
  const contentCache = new Map<string, { data: any; ts: number }>()

  function cacheKey(submissionId: number, fileId?: number): string {
    return `${submissionId}_${fileId ?? 0}`
  }

  function cacheGet(key: string): any | undefined {
    const entry = contentCache.get(key)
    if (entry && Date.now() - entry.ts < CACHE_TTL) return entry.data
    contentCache.delete(key)
    return undefined
  }

  function cacheSet(key: string, data: any) {
    contentCache.set(key, { data, ts: Date.now() })
  }

  function cacheClear(key: string) {
    contentCache.delete(key)
  }

  function downloadFile(submissionId: number, fileName: string, fileId?: number) {
    const a = document.createElement('a')
    const url = fileId != null
      ? `/api/submissions/${submissionId}/file?fileId=${fileId}`
      : `/api/submissions/${submissionId}/file`
    a.href = url
    a.download = fileName
    a.click()
  }

  async function previewFile(submissionId: number, fileName: string, contentType?: string, fileId?: number) {
    // 图片/视频 → 悬浮窗预览
    if (isMediaPreviewable(fileName)) {
      const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
      const IMAGE_EXTS = ['.png', '.jpg', '.jpeg', '.webp', '.gif', '.bmp']
      previewMode.value = IMAGE_EXTS.includes(ext) ? 'image' : 'video'
      currentFileId.value = fileId
      previewFileName.value = fileName
      previewContent.value = ''
      previewLoading.value = false
      previewError.value = ''
      previewVisible.value = true
      return
    }
    // Office 文档 → 转换后悬浮窗预览
    if (isOfficeFile(fileName)) {
      const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
      const key = cacheKey(submissionId, fileId)
      const cached = cacheGet(key)
      previewMode.value = 'office'
      currentFileId.value = fileId
      previewFileName.value = fileName
      previewError.value = ''
      previewContent.value = ''
      previewVisible.value = true
      if (cached) {
        previewLoading.value = false
        if (ext === '.xlsx') previewContent.value = await convertXlsx(cached)
        else officeBuffer.value = cached
        return
      }
      previewLoading.value = true
      try {
        const fetchUrl = fileId != null
          ? `/api/submissions/${submissionId}/file?fileId=${fileId}`
          : `/api/submissions/${submissionId}/file`
        const res = await fetch(fetchUrl, { credentials: 'include' })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const buffer = await res.arrayBuffer()
        cacheSet(key, buffer)
        if (ext === '.xlsx') {
          // XLSX → HTML string rendered in iframe
          previewContent.value = await convertXlsx(buffer)
          officeBuffer.value = null
        } else {
          // DOCX / PPTX → DOM render, store buffer for component
          officeBuffer.value = buffer
        }
      } catch (e: any) {
        previewError.value = e.message || 'Office 文件转换失败，请尝试下载'
        previewContent.value = ''
        officeBuffer.value = null
      } finally {
        previewLoading.value = false
      }
      return
    }

    // Markdown → 渲染后预览
    if (isMarkdownFile(fileName)) {
      const key = cacheKey(submissionId, fileId)
      const cached = cacheGet(key)
      previewMode.value = 'markdown'
      currentFileId.value = fileId
      previewFileName.value = fileName
      previewError.value = ''
      previewVisible.value = true
      if (cached) { previewLoading.value = false; previewContent.value = await getMarkdownHtml(cached); return }
      previewLoading.value = true
      try {
        const fetchUrl = fileId != null
          ? `/api/submissions/${submissionId}/file?fileId=${fileId}`
          : `/api/submissions/${submissionId}/file`
        const res = await fetch(fetchUrl, { credentials: 'include' })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const text = await res.text()
        cacheSet(key, text)
        previewContent.value = await getMarkdownHtml(text)
      } catch {
        previewError.value = '文件加载失败，请尝试下载'
        previewContent.value = ''
      } finally {
        previewLoading.value = false
      }
      return
    }

    // 文本/代码 → 悬浮窗内联预览
    if (!isTextPreviewable(fileName, contentType)) {
      downloadFile(submissionId, fileName, fileId)
      return
    }
    const key = cacheKey(submissionId, fileId)
    const cached = cacheGet(key)
    previewMode.value = 'text'
    currentFileId.value = fileId
    previewFileName.value = fileName
    previewError.value = ''
    previewVisible.value = true
    if (cached) { previewLoading.value = false; previewContent.value = cached; return }
    previewLoading.value = true
    try {
      const fetchUrl = fileId != null
        ? `/api/submissions/${submissionId}/file?fileId=${fileId}`
        : `/api/submissions/${submissionId}/file`
      const res = await fetch(fetchUrl, { credentials: 'include' })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const text = await res.text()
      cacheSet(key, text)
      previewContent.value = text
    } catch {
      previewError.value = '文件加载失败，请尝试下载'
      previewContent.value = ''
    } finally {
      previewLoading.value = false
    }
  }

  function closePreview() {
    previewVisible.value = false
    previewContent.value = ''
    previewFileName.value = ''
    previewError.value = ''
    previewMode.value = 'text'
    currentFileId.value = undefined
    officeBuffer.value = null
  }

  return { previewVisible, previewContent, previewFileName, previewLoading, previewError, previewMode, currentFileId, officeBuffer, downloadFile, previewFile, closePreview }
}
