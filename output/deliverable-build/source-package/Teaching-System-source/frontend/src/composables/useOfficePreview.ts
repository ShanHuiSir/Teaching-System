const OFFICE_EXTS = ['.docx', '.xlsx', '.pptx']

export function isOfficeFile(fileName: string): boolean {
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  return OFFICE_EXTS.includes(ext)
}

export async function renderDocx(container: HTMLElement, buffer: ArrayBuffer): Promise<void> {
  const { renderAsync } = await import('docx-preview')
  container.innerHTML = ''
  await renderAsync(buffer, container, null, {
    breakPages: true,
    ignoreWidth: true,
    ignoreHeight: true,
    renderHeaders: true,
    renderFooters: true,
  })
}

export async function renderPptx(container: HTMLElement, buffer: ArrayBuffer): Promise<void> {
  const { init } = await import('pptx-preview')

  // Render offscreen at library's native size to avoid DOM timing issues
  const offscreen = document.createElement('div')
  offscreen.style.cssText = 'position:fixed;left:-9999px;top:0;'
  document.body.appendChild(offscreen)

  try {
    const viewer = init(offscreen, { width: 960, height: 540 })
    await viewer.preview(buffer)

    // Extract rendered HTML and inject into real container with adaptive CSS
    const html = offscreen.innerHTML
    container.innerHTML = `
      <style>
        .pptx-adapt { max-width: 100%; overflow-x: auto; background: #fff; }
        .pptx-adapt * { max-width: 100%; box-sizing: border-box; }
        .pptx-adapt img { max-width: 100%; height: auto; }
      </style>
      <div class="pptx-adapt">${html}</div>`
  } finally {
    document.body.removeChild(offscreen)
  }
}

export async function renderOffice(container: HTMLElement, fileName: string, buffer: ArrayBuffer): Promise<void> {
  const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  switch (ext) {
    case '.docx':
      return renderDocx(container, buffer)
    case '.pptx':
      return renderPptx(container, buffer)
    default:
      throw new Error(`不支持的 Office 格式: ${ext}`)
  }
}

export async function convertXlsx(buffer: ArrayBuffer): Promise<string> {
  const XLSX = await import('xlsx')
  const workbook = XLSX.read(new Uint8Array(buffer), { type: 'array' })
  const sheets = workbook.SheetNames.map(name => {
    const sheet = workbook.Sheets[name]
    const html = XLSX.utils.sheet_to_html(sheet, { id: `sheet-${name}` })
    return `<h3>${name}</h3>${html}`
  })
  return `<style>
    body { font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; padding: 16px; color: #333; }
    table { border-collapse: collapse; width: 100%; font-size: 13px; }
    td, th { border: 1px solid #ddd; padding: 6px 10px; text-align: left; }
    th { background: #f5f5f5; font-weight: 600; }
    h3 { margin: 16px 0 8px; font-size: 15px; }
    hr { margin: 24px 0; border: none; border-top: 1px solid #eee; }
  </style>${sheets.join('<hr>')}`
}
