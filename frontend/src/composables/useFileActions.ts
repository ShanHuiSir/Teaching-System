import { ref } from 'vue'
import { useRouter } from 'vue-router'

const TEXT_PREVIEW_EXTENSIONS = [
  '.txt', '.log', '.csv', '.md', '.json', '.xml', '.yaml', '.yml',
  '.html', '.css', '.scss', '.less', '.js', '.ts', '.jsx', '.tsx', '.vue',
  '.java', '.py', '.cpp', '.c', '.h', '.hpp', '.cs', '.go', '.rs', '.rb',
  '.php', '.swift', '.kt', '.sql', '.sh', '.bat', '.ps1', '.ini', '.cfg',
  '.env', '.gitignore', '.properties', '.gradle', '.dockerfile',
]

const MEDIA_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.webp', '.gif', '.bmp', '.mp4', '.webm', '.mov', '.avi']

const TEXT_CONTENT_TYPES = ['text/', 'application/json', 'application/xml', 'application/javascript', 'application/x-sh']

function isTextPreviewable(fileName: string, contentType?: string): boolean {
  if (contentType && TEXT_CONTENT_TYPES.some(t => contentType.startsWith(t))) return true
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  return TEXT_PREVIEW_EXTENSIONS.includes(ext)
}

function isMediaPreviewable(fileName: string): boolean {
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  return MEDIA_EXTENSIONS.includes(ext)
}

export function isPreviewable(fileName: string, contentType?: string): boolean {
  return isTextPreviewable(fileName, contentType) || isMediaPreviewable(fileName)
}

export function useFileActions() {
  const router = useRouter()
  const previewVisible = ref(false)
  const previewContent = ref('')
  const previewFileName = ref('')
  const previewLoading = ref(false)
  const previewError = ref('')
  const previewMode = ref<'text' | 'image' | 'video'>('text')

  function downloadFile(submissionId: number, fileName: string) {
    const a = document.createElement('a')
    a.href = `/api/submissions/${submissionId}/file`
    a.download = fileName
    a.click()
  }

  async function previewFile(submissionId: number, fileName: string, contentType?: string) {
    // 图片/视频 → 悬浮窗预览
    if (isMediaPreviewable(fileName)) {
      const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
      const IMAGE_EXTS = ['.png', '.jpg', '.jpeg', '.webp', '.gif', '.bmp']
      previewMode.value = IMAGE_EXTS.includes(ext) ? 'image' : 'video'
      previewFileName.value = fileName
      previewContent.value = ''
      previewLoading.value = false
      previewError.value = ''
      previewVisible.value = true
      return
    }
    // 文本/代码 → 悬浮窗内联预览
    if (!isTextPreviewable(fileName, contentType)) {
      downloadFile(submissionId, fileName)
      return
    }
    previewMode.value = 'text'
    previewLoading.value = true
    previewFileName.value = fileName
    previewError.value = ''
    previewVisible.value = true
    try {
      const res = await fetch(`/api/submissions/${submissionId}/file`, { credentials: 'include' })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const text = await res.text()
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
  }

  return { previewVisible, previewContent, previewFileName, previewLoading, previewError, previewMode, downloadFile, previewFile, closePreview }
}
