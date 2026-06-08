export interface FileIconPaths {
  d: string
  fill?: string
  fillRule?: 'nonzero' | 'evenodd' | 'inherit'
  strokeDasharray?: string
}

export interface FileIconDef {
  viewBox: string
  paths: (string | FileIconPaths)[]
}

// SVG icon paths keyed by file type — used in ReviewPage & future file displays

export const FILE_ICONS: Record<string, FileIconDef> = {
  archive: {
    viewBox: '0 0 24 24',
    paths: [
      'M6 20.735a2 2 0 0 1 -1 -1.735v-14a2 2 0 0 1 2 -2h7l5 5v11a2 2 0 0 1 -2 2h-1',
      'M11 17a2 2 0 0 1 2 2v2a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1v-2a2 2 0 0 1 2 -2z',
      'M11 5h-1',
      'M13 7h-1',
      'M11 9h-1',
      'M13 11h-1',
      'M11 13h-1',
      'M13 15h-1',
    ],
  },
  docx: {
    viewBox: '0 0 24 24',
    paths: ['M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z', 'M14 2v6h6', 'M9 11v6l2-3 2 3v-6'],
  },
  xlsx: {
    viewBox: '0 0 24 24',
    paths: ['M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z', 'M14 2v6h6', 'M8 11h8M8 15h8M12 11v4'],
  },
  pptx: {
    viewBox: '0 0 24 24',
    paths: [
      'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z',
      'M14 2v6h6',
      'M8 12h8v5a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1z',
    ],
  },
  pdf: {
    viewBox: '0 0 24 24',
    paths: [
      'M13 9H18.5L13 3.5V9M6 2h8l6 6v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2m4.1 9.4c-.02.04-.29 1.76-2.1 4.69 0 0-3.5 1.82-2.67 3.18.67 1.08 2.32-.04 3.74-2.68 0 0 1.82-.64 4.24-.82 0 0 3.86 1.73 4.39-.11.52-1.86-3.06-1.44-3.7-1.25 0 0-2-.69-2.5-2.55 0 0 1.14-3.95-.61-3.9s-1.09 3.13-.79 4.1m.81 1.04c.03.01.47 1.21 1.89 2.46 0 0-2.33.46-3.39.9 0 0 1-1.73 1.5-3.36m3.93 2.72c.58-.16 2.33.15 2.26.48-.06.33-2.26-.48-2.26-.48M7.77 17c-.53 1.24-1.44 2-1.67 2-.23 0 .7-1.6 1.67-2m3.14-6.93c0-.07-.36-2.2 0-2.15.54.08 0 2.08 0 2.15z',
    ],
  },
  code: {
    viewBox: '0 0 24 24',
    paths: ['M16 18l6-6-6-6', 'M8 6l-6 6 6 6'],
  },
  text: {
    viewBox: '0 0 24 24',
    paths: ['M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z', 'M14 2v6h6'],
  },
  java: {
    viewBox: '0 0 24 24',
    paths: [
      'M8.84 19.9c.96.61 2.22.85 3.68.72c1.34-.12 2.27-.52 2.77-1.19c.28-.39.38-.87.26-1.32c-.14-.49-.52-.86-1.08-1.07c-.49-.18-1.15-.21-1.98-.07l-.41.07c-.72.13-1.26.15-1.66.06c-.28-.06-.5-.17-.64-.33c-.17-.22-.21-.49-.1-.83c.13-.44.48-.92 1.06-1.41c.42-.33.78-.59 1.08-.76c.48-.29.97-.65 1.46-1.09c.74-.65 1.28-1.36 1.58-2.14c.27-.71.35-1.45.23-2.2c-.14-.97-.59-1.77-1.38-2.38c-.71-.55-1.6-.87-2.65-.94c-1.4-.09-2.67.22-3.77.92l.46 1.47c.82-.55 1.7-.84 2.63-.88c.49-.01.89.08 1.16.29c.25.19.4.46.45.79c.05.29-.01.56-.17.81c-.18.27-.47.55-.86.83c-.33.23-.63.42-.91.58c-.59.36-1.11.77-1.56 1.24c-.64.67-1.07 1.38-1.28 2.14c-.15.55-.16 1.11-.02 1.64c.19.74.68 1.29 1.5 1.67z',
    ],
  },
  python: {
    viewBox: '0 0 24 24',
    paths: [
      'M12 2C7.58 2 4 3.79 4 6v3h8V8H6c0-1.1 2.69-2 6-2s6 .9 6 2v2h2V6c0-2.21-3.58-4-8-4z',
      'M4 10v3c0 2.21 3.58 4 8 4s8-1.79 8-4v-3h-8v1h6c0 1.1-2.69 2-6 2s-6-.9-6-2h2v-1H4z',
      'M12 22c4.42 0 8-1.79 8-4v-3h-8v1h6c0 1.1-2.69 2-6 2s-6-.9-6-2h2v2c0 2.21 3.58 4 8 4z',
      'M4 15v3c0 2.21 3.58 4 8 4v-2c-3.31 0-6-.9-6-2v-2h2v-1H4z',
    ],
  },
  cpp: {
    viewBox: '0 0 24 24',
    paths: ['M16 18l6-6-6-6', 'M8 6l-6 6 6 6', 'M12 9h.01M12 13h.01M15.5 9h.01M15.5 13h.01'],
  },
  golang: {
    viewBox: '0 0 24 24',
    paths: ['M12 4a8 8 0 1 0 0 16 8 8 0 0 0 0-16z', 'M8.5 8.5l7 7', 'M12 3v1.5M12 19.5V21M3 12h1.5M19.5 12H21'],
  },
  rust: {
    viewBox: '0 0 24 24',
    paths: [
      'M12 2l2.5 5.5L20 9l-3.5 4.5l.5 7L12 17.5L7 20.5l.5-7L4 9l5.5-1.5L12 2z',
      'M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z',
    ],
  },
  html: {
    viewBox: '0 0 24 24',
    paths: ['M4 5l2 14l6 2l6-2l2-14H4z', 'M8.5 10h7l-.5 4l-3 1l-3-1l-.2-1.5h1.5l.1.7l1.6.6l1.6-.6l.2-1.5H7.2l-.3-3h10.2l-.2 2H8.8z'],
  },
  js: {
    viewBox: '0 0 24 24',
    paths: ['M3 3h18v18H3V3z', 'M8.5 14.5v-1.5h2v4c0 1 .5 1.5 1.5 1.5s1.5-.5 1.5-1.5v-4h2v4c0 2-1.5 3-3.5 3s-3.5-1-3.5-3v-2.5z'],
  },
  css: {
    viewBox: '0 0 24 24',
    paths: ['M4 3l2 16l6 2l6-2l2-16H4z', 'M8 8h8l-.2 1.8H11l.3 2h4l-.5 4.5l-2.8.7l-2.8-.7l-.1-1.5h1.5l.1.7l1.3.4l1.3-.4l.1-1.5H8.5L8 8z'],
  },
  scss: {
    viewBox: '0 0 24 24',
    paths: ['M4 3l2 16l6 2l6-2l2-16H4z', 'M8 8h8l-.2 1.8H11l.3 2h4l-.5 4.5l-2.8.7l-2.8-.7l-.1-1.5h1.5l.1.7l1.3.4l1.3-.4l.1-1.5H8.5L8 8z'],
  },
  sass: {
    viewBox: '0 0 24 24',
    paths: ['M4 3l2 16l6 2l6-2l2-16H4z', 'M8 8h8l-.2 1.8H11l.3 2h4l-.5 4.5l-2.8.7l-2.8-.7l-.1-1.5h1.5l.1.7l1.3.4l1.3-.4l.1-1.5H8.5L8 8z'],
  },
  sql: {
    viewBox: '0 0 24 24',
    paths: [
      'M4 6c0 1.657 3.582 3 8 3s8-1.343 8-3s-3.582-3-8-3S4 4.343 4 6z',
      'M4 6v12c0 1.657 3.582 3 8 3s8-1.343 8-3V6',
      'M4 12c0 1.657 3.582 3 8 3s8-1.343 8-3',
    ],
  },
  kotlin: {
    viewBox: '0 0 24 24',
    paths: ['M5 20L15 12L5 4h4l10 8l-10 8H5z'],
  },
}

const extMap: Record<string, string> = {
  zip: 'archive',
  rar: 'archive',
  '7z': 'archive',
  tar: 'archive',
  gz: 'archive',
  docx: 'docx',
  doc: 'docx',
  xlsx: 'xlsx',
  xls: 'xlsx',
  pptx: 'pptx',
  ppt: 'pptx',
  pdf: 'pdf',
  py: 'python',
  java: 'java',
  c: 'cpp',
  cpp: 'cpp',
  h: 'cpp',
  hpp: 'cpp',
  go: 'golang',
  rs: 'rust',
  html: 'html',
  htm: 'html',
  js: 'js',
  jsx: 'js',
  ts: 'js',
  tsx: 'js',
  mjs: 'js',
  css: 'css',
  scss: 'scss',
  sass: 'sass',
  sql: 'sql',
  kt: 'kotlin',
  txt: 'text',
  md: 'text',
  json: 'text',
  xml: 'text',
  yaml: 'text',
  yml: 'text',
}

export function detectFileType(fileName: string, _contentType?: string): string {
  const ext = (fileName || '').split('.').pop()?.toLowerCase() || ''
  return extMap[ext] || 'text'
}
