// SVG icon paths keyed by file type — used in ReviewPage & future file displays

export const FILE_ICONS = {
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
    paths: [
      'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z',
      'M14 2v6h6',
      'M9 11v6l2-3 2 3v-6',
    ],
  },
  xlsx: {
    viewBox: '0 0 24 24',
    paths: [
      'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z',
      'M14 2v6h6',
      'M8 11h8M8 15h8M12 11v4',
    ],
  },
  pptx: {
    viewBox: '0 0 24 24',
    paths: [
      'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z',
      'M14 2v6h6',
      'M8 12h8v5a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1z', // simplified rect
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
    paths: [
      'M16 18l6-6-6-6',
      'M8 6l-6 6 6 6',
    ],
  },
  text: {
    viewBox: '0 0 24 24',
    paths: [
      'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z',
      'M14 2v6h6',
    ],
  },
  // Language-specific icons (V1 paths — raw SVG strings for direct use)
  java: `<path d="M11.506 23.363l1.287-.756-1.438.4..."/><path .../>`,  // placeholder, complex SVG
  python: `<circle cx="12" cy="12" r="10"/><path d="M8 10l4-4 4 4M8 14l4 4 4-4"/>`, // simplified
  cpp: `<path d="M435.28 113.28L247.19 4.69..."/><path .../>`, // placeholder, complex SVG
  golang: `<path d="M400.1 194.8C389.2 197.6..."/><path .../>`, // placeholder, complex SVG
  rust: `<path d="M15 3.77..."/><path .../>`, // placeholder, complex SVG
  html: `<path d="M2.072 0l2.399 26.963L15.234 30l10.837-3.037L28.472 0H2.072z M23.259 8.795H10.622l.281 3.393h12.074L22.04 22.389l-6.73 1.855v.02h-.075l-6.787-1.875-.413-5.213h3.281l.244 2.625 3.675.994 3.692-.994.414-4.275H7.866L6.984 5.514H23.56L23.259 8.795z"/>`,
  js: `<path d="M0 0h32v32H0zM29.38 24.37c-.234-1.464..."/><path d="M17.401 14.708H14.4..."/>`, // placeholder, complex SVG
  css: `<path d="M4.192 3.143h15.615l-1.42 16.034-6.404 1.812-6.369-1.813L4.192 3.143zM16.9 6.424H7.1l.158 1.949h7.529l-.189 2.022H9.66l.179 1.913h4.597l-.272 2.62-2.164.598-2.197-.603-.141-1.569H7.722l.216 2.867L12 17.484l3.995-1.138.905-9.922z"/>`,
  scss: `<path d="M16.171 18.7c-.481.221-1.008.509-2.063 1.088..."/><path d="M11.346 26.539..."/>`, // placeholder
  sass: `<path d="M15.092 22.146c-.015.03-.03.054 0 0zM27.548 17.796..."/><path d="M6.089 25.031..."/>`, // placeholder
  sql: `<path d="M64 16c-26.51 0-48 21.49-48 48s21.49 48 48 48..."/><path d="M105.29 87.19..."/>`, // placeholder
  kotlin: `<path d="M1.734 32l15.068-15.333L31.999 32zM0 0h16L0 16.667zM17.865 0L0 18.667V32L32 0z"/>`,
}

// Map file extension → icon key
export function detectFileType(fileName, contentType) {
  const ext = (fileName || '').split('.').pop().toLowerCase()
  const map = {
    zip: 'archive', rar: 'archive', '7z': 'archive', tar: 'archive', gz: 'archive',
    docx: 'docx', doc: 'docx',
    xlsx: 'xlsx', xls: 'xlsx',
    pptx: 'pptx', ppt: 'pptx',
    pdf: 'pdf',
    py: 'python',
    java: 'java',
    c: 'cpp', cpp: 'cpp', h: 'cpp', hpp: 'cpp',
    go: 'golang',
    rs: 'rust',
    html: 'html', htm: 'html',
    js: 'js', jsx: 'js', ts: 'js', tsx: 'js', mjs: 'js',
    css: 'css',
    scss: 'scss',
    sass: 'sass',
    sql: 'sql',
    kt: 'kotlin',
    txt: 'text', md: 'text', json: 'text', xml: 'text', yaml: 'text', yml: 'text',
  }
  return map[ext] || 'text'
}
