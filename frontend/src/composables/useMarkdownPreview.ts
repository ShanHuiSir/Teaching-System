let md: any = null

async function getParser(): Promise<any> {
  if (md) return md
  const [MarkdownIt, hljsModule] = await Promise.all([
    import('markdown-it').then(m => (m as any).default || m),
    import('highlight.js').then(m => m),
  ])
  const hljs: any = (hljsModule as any).default || hljsModule
  md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    breaks: true,
    highlight(code: string, lang: string): string {
      if (lang && hljs.getLanguage(lang)) {
        try {
          return `<pre><code class="hljs language-${lang}">${hljs.highlight(code, { language: lang }).value}</code></pre>`
        } catch { /* fall through */ }
      }
      return `<pre><code class="hljs">${md.utils.escapeHtml(code)}</code></pre>`
    },
  })
  return md
}

export function isMarkdownFile(fileName: string): boolean {
  return /\.md$/i.test(fileName)
}

async function renderMarkdown(text: string): Promise<string> {
  const parser = await getParser()
  return parser.render(text)
}

export async function getMarkdownHtml(text: string): Promise<string> {
  const body = await renderMarkdown(text)
  return `<style>
    .md-body {
      font-family: 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
      padding: 24px 32px;
      color: rgb(var(--md-sys-color-on-surface));
      line-height: 1.8;
      word-break: break-word;
    }
    .md-body h1, .md-body h2, .md-body h3, .md-body h4, .md-body h5, .md-body h6 {
      margin-top: 24px; margin-bottom: 12px;
      font-weight: 600; line-height: 1.4;
      color: rgb(var(--md-sys-color-on-surface));
    }
    .md-body h1 { font-size: 1.8em; border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant)); padding-bottom: 8px; }
    .md-body h2 { font-size: 1.5em; border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant)); padding-bottom: 6px; }
    .md-body h3 { font-size: 1.25em; }
    .md-body p { margin: 8px 0; }
    .md-body a { color: rgb(var(--md-sys-color-primary)); text-decoration: none; }
    .md-body a:hover { text-decoration: underline; }
    .md-body blockquote {
      margin: 12px 0; padding: 8px 16px;
      border-left: 4px solid rgb(var(--md-sys-color-primary));
      background: rgb(var(--md-sys-color-surface-container));
      color: rgb(var(--md-sys-color-on-surface-variant));
      border-radius: 0 8px 8px 0;
    }
    .md-body code {
      font-family: 'Cascadia Code', 'Consolas', 'Monaco', monospace;
      font-size: 0.9em;
      padding: 2px 6px;
      border-radius: 4px;
      background: rgb(var(--md-sys-color-surface-container-highest));
      color: rgb(var(--md-sys-color-on-surface));
    }
    .md-body pre {
      margin: 12px 0; padding: 16px;
      border-radius: 8px;
      background: rgb(var(--md-sys-color-surface-container-highest));
      overflow-x: auto;
    }
    .md-body pre code {
      padding: 0;
      background: transparent;
      font-size: 0.85em;
      line-height: 1.6;
    }
    .md-body table {
      width: 100%; border-collapse: collapse;
      margin: 12px 0;
    }
    .md-body th, .md-body td {
      border: 1px solid rgb(var(--md-sys-color-outline-variant));
      padding: 8px 12px; text-align: left;
    }
    .md-body th {
      background: rgb(var(--md-sys-color-surface-container));
      font-weight: 600;
    }
    .md-body img { max-width: 100%; border-radius: 8px; }
    .md-body ul, .md-body ol { padding-left: 24px; margin: 8px 0; }
    .md-body li { margin: 4px 0; }
    .md-body hr {
      border: none; border-top: 1px solid rgb(var(--md-sys-color-outline-variant));
      margin: 24px 0;
    }
    .md-body .hljs-keyword { color: rgb(var(--md-sys-color-primary)); }
    .md-body .hljs-string  { color: rgb(var(--md-sys-color-tertiary)); }
    .md-body .hljs-comment { color: rgb(var(--md-sys-color-on-surface-variant)); opacity: 0.6; }
    .md-body .hljs-number  { color: rgb(var(--md-sys-color-error)); }
    .md-body .hljs-title   { color: rgb(var(--md-sys-color-primary)); }
    .md-body .hljs-type    { color: rgb(var(--md-sys-color-tertiary)); }
    .md-body .hljs-built_in { color: rgb(var(--md-sys-color-secondary)); }
    .md-body .hljs-attr    { color: rgb(var(--md-sys-color-primary)); }
    .md-body .hljs-literal { color: rgb(var(--md-sys-color-error)); }
    .md-body .hljs-meta    { color: rgb(var(--md-sys-color-secondary)); }
  </style>
  <div class="md-body">${body}</div>`
}
